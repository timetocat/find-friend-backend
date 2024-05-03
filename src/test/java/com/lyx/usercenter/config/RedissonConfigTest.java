package com.lyx.usercenter.config;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author timecat
 * @create
 */
@SpringBootTest
class RedissonConfigTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {

        RList<Object> rList = redissonClient.getList("test-list");
        rList.add("lyx test");
        System.out.println("rList: " + rList.get(0));
        rList.remove(0);
    }
}