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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lyx.usercenter.constant.RedisKeys.PRIVATE_CHAT_KEY;
import static com.lyx.usercenter.constant.RedisKeys.TEAM_CHAT_KEY;
import static com.lyx.usercenter.model.enums.ChatScopeIntEnum.PRIVATE_CHAT;

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
        Function<ChatScopeIntEnum, String> getRedisKey = PRIVATE_CHAT.equals(chatScopeEnum) ?
                scope -> String.format(PRIVATE_CHAT_KEY + "%s", key) :
                scope -> String.format(TEAM_CHAT_KEY + "%s", key);
        return getRedisKey.toString();
    }
}




