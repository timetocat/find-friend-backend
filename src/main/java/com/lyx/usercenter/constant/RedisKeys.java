package com.lyx.usercenter.constant;

/**
 * Redis 缓存key
 *
 * @author timecat
 * @create
 */
public interface RedisKeys {

    /**
     * user相关
     */
    String FIND_FRIENDS_USER = "findFriends:user:";
    /**
     * 首页推荐缓存key prefix
     */
    String INDEX_RECOMMEND = FIND_FRIENDS_USER + "recommend:";
    /**
     * 首页推举缓存预热获取分布式锁key
     */
    String INDEX_RECOMMEND_DISTRIBUTED_LOCK = FIND_FRIENDS_USER + "recommend:distributedLock";

    /**
     * team 相关
     */
    String FIND_FRIENDS_TEAM = "findFriends:team:";
    /**
     * 创建队伍锁key prefix
     */
    String ADD_TEAM_LOCK = FIND_FRIENDS_TEAM + "add:";
    /**
     * 加入队伍分布式锁key prefix
     */
    String JOIN_TEAM_DISTRIBUTED_LOCK = FIND_FRIENDS_TEAM + "join:";


    /**
     * friend 相关
     */
    String FIND_FRIENDS_FRIEND = "find:friends:friend:";
    /**
     * 添加好友
     */
    String ADD_FRIEND = FIND_FRIENDS_FRIEND + "add:";
}
