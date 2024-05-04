package com.lyx.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.lyx.usercenter.constant.RedisKeys.INDEX_RECOMMEND;
import static com.lyx.usercenter.constant.RedisKeys.INDEX_RECOMMEND_DISTRIBUTED_LOCK;

/**
 * 缓存预热
 *
 * @author timecat
 * @create
 */
@Slf4j
@Component
public class PreCacheJob {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L, 2L);

    // 每天执行，预热推荐用户
    @Scheduled(cron = "0 0 0 * * *")
    public void preCacheRecommendUser() {
        RLock lock = redissonClient.getLock(INDEX_RECOMMEND_DISTRIBUTED_LOCK);
        try {
            // 只有一个线程能获取锁
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) { // 释放时间为-1是为了开启看门狗机制
                log.info("getLock: " + Thread.currentThread().getName());
                // 查询数据库
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                String redisKey = String.format(INDEX_RECOMMEND + "%s", mainUserList);
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                // 写缓存，30s 过期
                try {
                    valueOperations.set(redisKey, userPage, 30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error("redis set key error", e);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCache Recommend error", e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.info("unlock: " + Thread.currentThread().getName());
                lock.unlock();
            }
        }

    }

}
