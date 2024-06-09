package com.lyx.usercenter.ws;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lyx.usercenter.config.HttpSessionConfig;
import com.lyx.usercenter.model.domain.Chat;
import com.lyx.usercenter.model.domain.Team;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.enums.BaseIntEnum;
import com.lyx.usercenter.model.enums.ChatScopeIntEnum;
import com.lyx.usercenter.model.request.chat.MessageRequest;
import com.lyx.usercenter.model.vo.ChatUserInfo;
import com.lyx.usercenter.model.vo.MessageVO;
import com.lyx.usercenter.service.ChatService;
import com.lyx.usercenter.service.TeamService;
import com.lyx.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.lyx.usercenter.constant.RedisKeys.NULL_KEY;
import static com.lyx.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.lyx.usercenter.constant.UserConstant.USER_LOGIN_STATE;
import static com.lyx.usercenter.model.enums.ChatScopeIntEnum.*;

/**
 * WebSocket
 *
 * @author timecat
 * @create
 */
@Slf4j
@Component
@ServerEndpoint(value = "/websocket/{userId}/{teamId}", configurator = HttpSessionConfig.class)
public class WebSocket {

    /**
     * 保存队伍的连接信息
     */
    private static final Map<String, ConcurrentHashMap<String, WebSocket>> ROOMS = new HashMap<>();
    /**
     * 线程安全的无序的集合
     */
    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();
    /**
     * 存储在线连接数
     */
    private static final Map<String, Session> SESSION_POOL = new HashMap<>(0);
    private static UserService userService;
    private static ChatService chatService;
    private static TeamService teamService;
    /**
     * 房间在线人数
     */
    private static int onlineCount = 0;
    /**
     * 当前信息
     */
    private Session session;
    private HttpSession httpSession;

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }

    @Resource
    public void setHeatMapService(UserService userService) {
        WebSocket.userService = userService;
    }

    @Resource
    public void setHeatMapService(ChatService chatService) {
        WebSocket.chatService = chatService;
    }

    @Resource
    public void setHeatMapService(TeamService teamService) {
        WebSocket.teamService = teamService;
    }


    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId,
                       @PathParam(value = "teamId") String teamId, EndpointConfig config) {

        try {
            if (StrUtil.isBlank(userId) || "undefined".equals(userId)) {
                sendError(userId, "参数错误");
                return;
            }
            HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
            User user = (User) httpSession.getAttribute(USER_LOGIN_STATE);
            if (user != null) {
                this.session = session;
                this.httpSession = httpSession;
            }
            if (!"NaN".equals(teamId)) {
                if (!ROOMS.containsKey(teamId)) {
                    ConcurrentHashMap<String, WebSocket> room = new ConcurrentHashMap<>();
                    room.put(userId, this);
                    ROOMS.put(String.valueOf(teamId), room);
                    // 在线数 +1
                    addOnlineCount();
                } else {
                    if (!ROOMS.get(teamId).containsKey(userId)) {
                        ROOMS.get(teamId).put(userId, this);
                        // 在线数 +1
                        addOnlineCount();
                    }
                }
                log.info("有新连接加入，当前在线人数为：" + getOnlineCount());
            } else {
                SESSIONS.add(session);
                SESSION_POOL.put(userId, session);
                log.info("有新用户加入，userId={},当前在线人数：{}", userId, SESSION_POOL.size());
                // sendAllUsers();
            }
        } catch (Exception e) {
            log.error("连接失败！", e);
            sendError(userId, e.getMessage());
        }
    }

    /**
     * 发送给所有在线用户
     */
    /*private void sendAllUsers() {
        log.info("【WebSocket消息】发送所有在线用户信息");
        HashMap<String, List<ChatUserInfo>> stringListHashMap = new HashMap<>(0);
        List<ChatUserInfo> chatUserInfos = new ArrayList<>();
        for (Serializable key : SESSION_POOL.keySet()) {
            User user = userService.getById(key);
            ChatUserInfo chatUserInfo = new ChatUserInfo();
            BeanUtils.copyProperties(user, chatUserInfo);
            chatUserInfos.add(chatUserInfo);
        }
        stringListHashMap.put("users", chatUserInfos);
        sendAllMessage(JSONUtil.toJsonStr(stringListHashMap), null);
    }*/

    @OnClose
    public void onClose(Session session, @PathParam(value = "userId") String userId,
                        @PathParam(value = "teamId") String teamId) {
        try {
            if (!"NaN".equals(teamId)) {
                ROOMS.get(teamId).remove(userId);
                if (getOnlineCount() > 0) {
                    subOnlineCount();
                }
                log.info("有用户退出，当前在线人数为：" + getOnlineCount());
            } else {
                if (!SESSION_POOL.isEmpty()) {
                    SESSION_POOL.remove(userId);
                    SESSIONS.remove(session);
                }
                log.info("[WebSocket消息] 连接断开，当前总数为：" + SESSION_POOL.size());
                // sendAllUsers();
            }
        } catch (Exception e) {
            log.error("断开连接失败！", e);
        }
    }

    @OnMessage
    public void onMessage(String message, @PathParam(value = "userId") String userId) {
        if ("PING".equals(message)) {
            sendOneMessage(userId, "pong");
            log.warn("心跳包，发送给{},在线：{}人", userId, getOnlineCount());
            return;
        }
        if (StrUtil.isBlank(userId) || "undefined".equals(userId)
                || userService.getById(userId) == null) {
            sendError(userId, "参数错误");
            return;
        }
        log.info("服务端收到用户{}的消息：{}", userId, message);
        MessageRequest messageRequest = JSONUtil.toBean(message, MessageRequest.class);
        if (messageRequest == null) {
            sendError(userId, "参数错误");
            return;
        }
        Integer scope = messageRequest.getScope();
        ChatScopeIntEnum scopeEnum = BaseIntEnum.getEnumByValue(ChatScopeIntEnum.class, scope);
        if (scopeEnum == null) {
            sendError(userId, "聊天类型参数错误");
            return;
        }
        if (PRIVATE_CHAT.equals(scopeEnum)) {
            // 私聊
            privateChat(messageRequest, Long.valueOf(userId));
        } else if (TEAM_CHAT.equals(scopeEnum)) {
            // 队伍聊天
            teamChat(messageRequest, Long.valueOf(userId));
        } else {
            // 大厅聊天
            hallChat(messageRequest, Long.valueOf(userId));
        }

    }

    /**
     * 大厅聊天
     *
     * @param messageRequest
     * @param userId
     */
    private void hallChat(MessageRequest messageRequest, Long userId) {
        MessageVO messageVO = setChatResult(userId, messageRequest);
        User fromUser = userService.getById(userId);
        if (fromUser.getUserRole() == ADMIN_ROLE) {
            messageVO.setIsAdmin(true);
        }
        String jsonStr = JSONUtil.toJsonStr(messageVO);
        sendAllMessage(jsonStr, userId);
        saveChat(messageVO);
        chatService.deleteKey(HALL_CHAT, NULL_KEY);
    }

    /**
     * 广播消息
     *
     * @param message
     */
    private void sendAllMessage(String message, Long userId) {
        log.info("[WebSocket消息] 广播消息：" + message);
        AtomicBoolean flag = new AtomicBoolean(false);
        if (userId == null) {
            flag.set(true);
        }
        for (Session session : SESSIONS) {
            try {
                flag.set(!session.getPathParameters().get("userId")
                        .equals(String.valueOf(userId)));
                if (flag.get() && session.isOpen()) {
                    synchronized (session) {
                        session.getBasicRemote().sendText(message);
                    }
                }
            } catch (Exception e) {
                log.error("广播消息失败！", e);
            }
        }
    }

    /**
     * 队伍聊天
     *
     * @param messageRequest
     * @param userId
     */
    private void teamChat(MessageRequest messageRequest, Long userId) {
        MessageVO messageVO = setChatResult(userId, messageRequest);
        User fromUser = userService.getById(userId);
        Team team = teamService.getById(messageRequest.getTeamId());
        boolean isAdmin = ADMIN_ROLE == fromUser.getUserRole()
                || Objects.equals(team.getUserId(), userId);
        if (isAdmin) {
            messageVO.setIsAdmin(true);
        }
        String jsonStr = JSONUtil.toJsonStr(messageVO);
        try {
            broadcast(String.valueOf(team.getId()), jsonStr, userId);
            saveChat(messageVO);
            chatService.deleteKey(TEAM_CHAT, String.valueOf(team.getId()));
            log.info("{}在id为{}的{}队伍内聊天，在线{}人",
                    fromUser.getUsername(), team.getId(), team.getName(), getOnlineCount());
        } catch (Exception e) {
            log.error("队伍聊天异常!", e);
        }
    }

    /**
     * 队伍内群发消息
     *
     * @param teamId
     * @param message
     */
    private void broadcast(String teamId, String message, Long userId) {
        ConcurrentHashMap<String, WebSocket> map = ROOMS.get(teamId);
        map.forEach((key, value) -> {
            if (!key.equals(String.valueOf(userId))) {
                value.sendMessage(message);
            }
        });
    }

    /**
     * 发送消息
     *
     * @param message
     */
    private void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("发送消息失败！", e);
        }
    }

    /**
     * 私聊
     *
     * @param messageRequest
     * @param userId
     */
    private void privateChat(MessageRequest messageRequest, Long userId) {
        Long toId = messageRequest.getToId();
        boolean result = userService.isFriend(userId, toId);
        if (!result) {
            sendError(String.valueOf(userId), "用户不是好友");
            return;
        }
        Session session = SESSION_POOL.get(String.valueOf(toId));
        MessageVO messageVO = setChatResult(userId, messageRequest);
        if (session != null) {
            messageVO.setIsMy(checkIsMe(messageRequest.getToId()));
            messageVO.setIsAdmin(userService.getById(userId).getUserRole().equals(ADMIN_ROLE));
            String jsonStr = JSONUtil.toJsonStr(messageVO);
            sendOneMessage(String.valueOf(toId), jsonStr);
            log.info("发送给用户{}，消息：{}", messageVO.getToUser().getUsername(), jsonStr);
        } else {
            log.info("用户不在线{}的session", toId);
        }
        // 保存信息
        saveChat(messageVO);
        // 需要刷新最新消息
        chatService.deleteKey(PRIVATE_CHAT, String.format("%dto%d", userId, toId));
        chatService.deleteKey(PRIVATE_CHAT, String.format("%dto%d", toId, userId));
    }

    /**
     * 判断发送消息用户是否是自己
     *
     * @param userId
     * @return
     */
    private boolean checkIsMe(long userId) {
        User loginUser = (User) this.httpSession.getAttribute(USER_LOGIN_STATE);
        return Objects.equals(loginUser.getId(), userId);
    }

    /**
     * 保存聊天信息
     *
     * @param messageVO
     */
    private void saveChat(MessageVO messageVO) {
        Chat chat = BeanUtil.copyProperties(messageVO, Chat.class);
        chat.setFromId(messageVO.getFromUser().getUserId());
        if (messageVO.getToUser() != null) {
            chat.setToId(messageVO.getToUser().getUserId());
        }
        Date dateTime = DateUtil
                .parse(messageVO.getCreateTime(), "yyyy年MM月dd日 HH:mm:ss");
        chat.setCreateTime(dateTime);
        chatService.save(chat);
    }

    private MessageVO setChatResult(Long userId, MessageRequest messageRequest) {
        MessageVO messageVO = BeanUtil.copyProperties(messageRequest, MessageVO.class);
        User fromUser = userService.getById(userId);
        User toUser = userService.getById(messageRequest.getToId());
        ChatUserInfo fromChatUserInfo = BeanUtil.copyProperties(fromUser, ChatUserInfo.class);
        ChatUserInfo toChatUserInfo = BeanUtil.copyProperties(toUser, ChatUserInfo.class);
        messageVO.setFromUser(fromChatUserInfo);
        messageVO.setToUser(toChatUserInfo);
        messageVO.setCreateTime(DateUtil.format(DateTime.now()
                , "yyyy年MM月dd日 HH:mm:ss"));
        return messageVO;
    }


    /**
     * 发送失败
     *
     * @param userId
     * @param errorMessage
     */
    private void sendError(String userId, String errorMessage) {
        JSONObject json = new JSONObject();
        json.set("error", errorMessage);
        sendOneMessage(userId, json.toString());
    }

    /**
     * 单点消息
     *
     * @param userId
     * @param message
     */
    private void sendOneMessage(String userId, String message) {
        Session session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    log.info("[WebSocket消息] 单点消息" + message);
                    session.getAsyncRemote()
                            .sendText(message);
                }
            } catch (Exception e) {
                log.error("单点消息发送失败！");
            }
        }
    }

}
