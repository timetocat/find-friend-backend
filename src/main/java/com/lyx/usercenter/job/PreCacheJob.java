package com.lyx.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyx.usercenter.constant.RedisKeys;
import com.lyx.usercenter.model.User;
import com.lyx.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L, 2L);

    // 每天执行，预热推荐用户
    @Scheduled(cron = "0 12 1 * * *")
    public void preCacheRecommendUser() {
        // 查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
        String redisKey = String.format(RedisKeys.INDEX_RECOMMEND + "%s", mainUserList);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 写缓存，30s 过期
        try {
            valueOperations.set(redisKey, userPage, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }

    }

}
