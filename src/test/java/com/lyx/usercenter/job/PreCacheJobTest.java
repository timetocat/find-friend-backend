package com.lyx.usercenter.job;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author timecat
 * @create
 */
@SpringBootTest
class PreCacheJobTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void testWatchDog() {
        RLock lock = redissonClient.getLock("findFriend:test:watchDog");
        // 只有一个线程能获取锁
        try {
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
                Thread.sleep(120000);
                System.out.println("getLock: " + Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println(("unlock: " + Thread.currentThread().getName()));
                lock.unlock();
            }
        }
    }
}