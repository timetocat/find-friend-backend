package com.lyx.usercenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.mapper.ChatMapper;
import com.lyx.usercenter.model.domain.Chat;
import com.lyx.usercenter.model.domain.Team;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.enums.ChatScopeIntEnum;
import com.lyx.usercenter.model.request.chat.ChatRequest;
import com.lyx.usercenter.model.vo.ChatUserInfo;
import com.lyx.usercenter.model.vo.MessageVO;
import com.lyx.usercenter.service.ChatService;
import com.lyx.usercenter.service.TeamService;
import com.lyx.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.lyx.usercenter.constant.RedisKeys.*;
import static com.lyx.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.lyx.usercenter.model.enums.ChatScopeIntEnum.*;

/**
 * @author timecat
 * @description 针对表【chat(消息表)】的数据库操作Service实现
 * @createDate 2024-05-12 21:57:30
 */
@Slf4j
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
        implements ChatService {

    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;
    @Resource
    private RedisTemplate<String, List<MessageVO>> redisTemplate;

    @Override
    public List<MessageVO> getPrivateChat(ChatRequest chatRequest, User loginUser) {
        Long toId = chatRequest.getToId();
        Long userId = loginUser.getId();
        // 判断是否有私聊用户
        if (toId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "未正确私聊用户");
        }
        // 先查询缓存中是否有私聊信息
        String key = String.format("%dto%d", toId, userId);
        List<MessageVO> chatRecordsCache = getCache(PRIVATE_CHAT, key);
        if (chatRecordsCache != null) {
            // 刷新缓存有效期
            saveCache(PRIVATE_CHAT, key, chatRecordsCache);
            return chatRecordsCache;
        }
        // 从数据库中查询私聊信息
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.and(privateChat ->
                privateChat.eq(Chat::getFromId, userId).eq(Chat::getToId, toId)
                        .or().
                        eq(Chat::getFromId, toId).eq(Chat::getToId, userId)
        ).eq(Chat::getScope, PRIVATE_CHAT.getValue());
        // 两方共用的聊天信息
        List<Chat> list = this.list(chatLambdaQueryWrapper);
        List<MessageVO> messageVOList = list.stream().map(chat -> {
            MessageVO messageVO = chatResult(userId, toId, chat);
            if (chat.getFromId().equals(userId)) {
                messageVO.setIsMy(true);
            }
            return messageVO;
        }).collect(Collectors.toList());
        saveCache(PRIVATE_CHAT, key, messageVOList);
        return messageVOList;
    }

    @Override
    public MessageVO chatResult(Long userId, Long toId, Chat chat) {
        MessageVO messageVO = BeanUtil.copyProperties(chat, MessageVO.class);
        User fromUser = userService.getById(userId);
        User toUser = userService.getById(toId);
        ChatUserInfo fromChatUserInfo = BeanUtil.copyProperties(fromUser, ChatUserInfo.class);
        ChatUserInfo toChatUserInfo = BeanUtil.copyProperties(toUser, ChatUserInfo.class);
        messageVO.setFromUser(fromChatUserInfo);
        messageVO.setToUser(toChatUserInfo);
        messageVO.setCreateTime(DateUtil.format(chat.getCreateTime(), "yyyy年MM月dd日 HH:mm:ss"));
        return messageVO;
    }

    @Override
    public MessageVO getHallMessage(Chat chat) {
        User fromUser = userService.getById(chat.getFromId());
        ChatUserInfo fromChatUserInfo = BeanUtil.copyProperties(fromUser, ChatUserInfo.class);
        MessageVO messageVO = BeanUtil.copyProperties(chat, MessageVO.class);
        messageVO.setFromUser(fromChatUserInfo);
        return messageVO;
    }

    @Override
    public List<MessageVO> getHallChat(User loginUser) {
        List<MessageVO> chatRecordsCache = getCache(HALL_CHAT, NULL_KEY);
        if (chatRecordsCache != null) {
            chatRecordsCache = checkIsMyMessage(chatRecordsCache, loginUser);
            // 刷新缓存有效期
            saveCache(HALL_CHAT, NULL_KEY, chatRecordsCache);
            return chatRecordsCache;
        }
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getScope, HALL_CHAT.getValue());
        List<MessageVO> chatRecords = getMessageList(loginUser, null, chatLambdaQueryWrapper);
        saveCache(HALL_CHAT, NULL_KEY, chatRecords);
        return chatRecords;
    }

    @Override
    public List<MessageVO> getTeamChat(Long teamId, User loginUser) {
        if (teamId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求错误");
        }
        Long userId = loginUser.getId();
        // 判断当前用户是否属于该队伍
        boolean checkJoinTeam = teamService.checkJoinTeam(userId, teamId);
        if (!checkJoinTeam) {
            throw new BusinessException(ErrorCode.NO_AUTH, "未加入该队伍");
        }
        List<MessageVO> chatRecordsCache = getCache(TEAM_CHAT, String.valueOf(teamId));
        if (chatRecordsCache != null) {
            chatRecordsCache = checkIsMyMessage(chatRecordsCache, loginUser);
            // 刷新缓存有效期
            saveCache(TEAM_CHAT, String.valueOf(teamId), chatRecordsCache);
            return chatRecordsCache;
        }
        // todo 管理员是否有权查看队伍聊天室信息？
        Team team = teamService.getById(teamId);
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getTeamId, teamId)
                .eq(Chat::getScope, TEAM_CHAT.getValue());
        List<MessageVO> chatRecords = getMessageList(loginUser, team.getUserId(), chatLambdaQueryWrapper);
        saveCache(TEAM_CHAT, String.valueOf(teamId), chatRecords);
        return chatRecords;
    }

    @Override
    public void deleteKey(ChatScopeIntEnum chatScopeEnum, String key) {
        String chatRedisKey = getChatRedisKey(chatScopeEnum, key);
        redisTemplate.delete(chatRedisKey);
    }


    /**
     * 获取大厅或队伍消息
     *
     * @param loginUser
     * @param userId
     * @param chatLambdaQueryWrapper
     * @return
     */
    private List<MessageVO> getMessageList(User loginUser, Long userId, LambdaQueryWrapper<Chat> chatLambdaQueryWrapper) {
        List<Chat> list = this.list(chatLambdaQueryWrapper);
        return list.stream().map(chat -> {
            MessageVO messageVO = getHallMessage(chat);
            boolean isCaptain = userId != null && userId.equals(chat.getFromId());
            Integer userRole = userService.getById(chat.getFromId()).getUserRole();
            // 在大厅中设置消息是否为管理员发送
            // 在队伍中设置消息是否为队长发送
            if (userRole == ADMIN_ROLE || isCaptain) {
                messageVO.setIsAdmin(true);
            }
            if (chat.getFromId().equals(loginUser.getId())) {
                messageVO.setIsMy(true);
            }
            messageVO.setCreateTime(DateUtil.format(chat.getCreateTime(),
                    "yyyy年MM月dd日 HH:mm:ss"));
            return messageVO;
        }).collect(Collectors.toList());
    }

    private List<MessageVO> checkIsMyMessage(List<MessageVO> chatRecordsCache, User loginUser) {
        Long userId = loginUser.getId();
        return chatRecordsCache.stream().peek(chat -> {
            // 发送信息不是我
            if (!Objects.equals(chat.getFromUser().getUserId(), userId)
                    && Boolean.TRUE.equals(chat.getIsMy())) {
                chat.setIsMy(false);
            }
            // 发送信息是我
            if (Objects.equals(chat.getFromUser().getUserId(), userId)
                    && Boolean.TRUE.equals(!chat.getIsMy())) {
                chat.setIsMy(true);
            }
        }).collect(Collectors.toList());
    }

    /**
     * 保存聊天记录缓存
     *
     * @param chatScopeEnum
     * @param key
     * @param chatRecordsCache
     */
    private void saveCache(ChatScopeIntEnum chatScopeEnum, String key, List<MessageVO> chatRecordsCache) {
        // 解决缓存雪崩
        final long TWO_MINUTES_SECONDS = TimeUnit.SECONDS.convert(2L, TimeUnit.MINUTES);
        long seconds = RandomUtil.randomLong(1, 31);
        try {
            redisTemplate.opsForValue().set(getChatRedisKey(chatScopeEnum, key), chatRecordsCache,
                    TWO_MINUTES_SECONDS + seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error saving chat cache", e);
        }
    }

    /**
     * 获取聊天信息缓存
     *
     * @param chatScopeEnum
     * @param key
     * @return
     */
    private List<MessageVO> getCache(ChatScopeIntEnum chatScopeEnum, String key) {
        // 查询缓存中的私聊信息
        return redisTemplate.opsForValue().get(getChatRedisKey(chatScopeEnum, key));
    }

    /**
     * 获取聊天信息的缓存key
     *
     * @param chatScopeEnum
     * @param key
     * @return
     */
    private String getChatRedisKey(ChatScopeIntEnum chatScopeEnum, String key) {
        UnaryOperator<String> getRedisKey = redisKeyMap.get(chatScopeEnum);
        return getRedisKey.apply(key);
    }

    private static final Map<ChatScopeIntEnum, UnaryOperator<String>> redisKeyMap = new EnumMap<>(ChatScopeIntEnum.class);

    static {
        redisKeyMap.put(PRIVATE_CHAT, key -> String.format(PRIVATE_CHAT_KEY + "%s", key));
        redisKeyMap.put(TEAM_CHAT, key -> String.format(TEAM_CHAT_KEY + "%s", key));
        redisKeyMap.put(HALL_CHAT, key -> String.format(HALL_CHAT_KEY));
    }
}
