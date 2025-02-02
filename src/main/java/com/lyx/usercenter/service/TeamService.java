package com.lyx.usercenter.service;

import com.lyx.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.dto.TeamQuery;
import com.lyx.usercenter.model.request.team.TeamJoinRequest;
import com.lyx.usercenter.model.request.team.TeamQuitRequest;
import com.lyx.usercenter.model.request.team.TeamUpdateRequest;
import com.lyx.usercenter.model.vo.TeamUserVO;

import java.util.List;

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

    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeam(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     *
     * @param teamInfo
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamInfo, User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinInfo
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinInfo, User loginUser);

    /**
     * 退出队伍
     *
     * @param teamQuitInfo
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitInfo, User loginUser);

    /**
     * 手动解散队伍
     *
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id, User loginUser);

    /**
     * 解散队伍
     *
     * @param id
     * @return
     */
    boolean disbandTeam(long id);

    /**
     * 判断是否加入队伍
     *
     * @param userId
     * @param teamId
     * @return
     */
    boolean checkJoinTeam(Long userId, Long teamId);
}
