package com.lyx.usercenter.constant;

/**
 * Redis 缓存key
 *
 * @author timecat
 * @create
 */
public interface RedisKeys {

    /**
     * 首页推荐缓存key prefix
     */
    String INDEX_RECOMMEND = "findFriends:user:recommend:";
    /**
     * 首页推举缓存预热获取分布式锁key
     */
    String INDEX_RECOMMEND_DISTRIBUTED_LOCK = "findFriends:user:recommend:distributedLock";
    /**
     * 创建队伍锁key prefix
     */
    String ADD_TEAM_LOCK = "findFriends:team:add:";
    /**
     * 加入队伍分布式锁key prefix
     */
    String JOIN_TEAM_DISTRIBUTED_LOCK = "findFriends:team:join:";
}
