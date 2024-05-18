package com.lyx.usercenter.service;

import com.lyx.usercenter.model.domain.UserTeam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author timecat
 * @description 针对表【user_team(用户队伍关系)】的数据库操作Service
 * @createDate 2024-05-03 19:54:40
 */
public interface UserTeamService extends IService<UserTeam> {

    /**
     * 获取曾经退出队伍的信息
     *
     * @param userId
     * @param teamId
     * @return
     */
    List<Long> getBeforeDate(long userId, long teamId);

    /**
     * 再次加入队伍接触逻辑删除
     *
     * @param userId
     * @param teamId
     * @return
     */
    boolean updateDeleteByJoin(long userId, long teamId);

    /**
     * 判断用户是否加入该队伍
     *
     * @param userId
     * @param teamId
     * @return
     */
    boolean checkJoinTeam(long userId, long teamId);

}
