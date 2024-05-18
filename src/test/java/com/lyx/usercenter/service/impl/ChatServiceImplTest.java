package com.lyx.usercenter.service.impl;

import com.lyx.usercenter.constant.RedisKeys;
import com.lyx.usercenter.model.enums.ChatScopeIntEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.lyx.usercenter.constant.RedisKeys.*;
import static com.lyx.usercenter.model.enums.ChatScopeIntEnum.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author timecat
 * @create
 */
class ChatServiceImplTest {

    @Test
    void getChatRedisKey() {
        // 为了方便测试，变成静态方法
/*        String chatRedisKey1 = ChatServiceImpl.getChatRedisKey(PRIVATE_CHAT, "test1");
        String chatRedisKey2 = ChatServiceImpl.getChatRedisKey(TEAM_CHAT, "test2");
        String chatRedisKey3 = ChatServiceImpl.getChatRedisKey(HALL_CHAT, "test3");
        Assertions.assertEquals(PRIVATE_CHAT_KEY + "test1", chatRedisKey1);
        Assertions.assertEquals(TEAM_CHAT_KEY + "test2", chatRedisKey2);
        Assertions.assertEquals(HALL_CHAT_KEY, chatRedisKey3);*/
    }
}