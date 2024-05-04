package com.lyx.usercenter.service;

import com.lyx.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyx.usercenter.model.domain.User;

/**
 * @author timecat
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2024-05-03 19:54:23
 */
public interface TeamService extends IService<Team> {
    /**
     * 添加队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);
}
