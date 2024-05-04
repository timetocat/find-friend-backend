package com.lyx.usercenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.mapper.TeamMapper;
import com.lyx.usercenter.model.domain.Team;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.domain.UserTeam;
import com.lyx.usercenter.model.dto.TeamQuery;
import com.lyx.usercenter.model.enums.TeamStatusEnum;
import com.lyx.usercenter.model.request.TeamJoinRequest;
import com.lyx.usercenter.model.request.TeamUpdateRequest;
import com.lyx.usercenter.model.vo.TeamUserVO;
import com.lyx.usercenter.model.vo.UserVO;
import com.lyx.usercenter.service.TeamService;
import com.lyx.usercenter.service.UserService;
import com.lyx.usercenter.service.UserTeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author timecat
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-05-03 19:54:23
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 2. 是否登录，未登录不允许创建队伍
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        // 3. 校验信息
        //  a. 队伍人数 (1,20]
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        //  b. 队伍标题字数 <= 20
        String name = team.getName();
        if (StrUtil.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不符合要求");
        }
        //  c. 队伍描述字数 <= 1000 (数据库是1024)
        String description = team.getDescription(); // 描述可以为空
        if (StrUtil.isNotBlank(description) && description.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不符合要求");
        }
        //  d. status 是否公开，未传参数默认为公开（0）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不符合要求");
        }
        //  e. status 为加密，一定要有密码，且密码 <= 32位
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StrUtil.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不符合要求");
            }
        }
        //  f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍超时时间 < 当前时间");
        }
        //  g. 用户最多创建 5 个队伍
        // todo 有bug，用户可能同时创建超过额定限制（比如同时插入100个队伍）
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        long hasTeamNum = this.count();
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        // 4. 插入队伍信息 => 队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        // 5. 插入用户 => 用户队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeam(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查询条件（根据什么查询）
        if (teamQuery != null) {
            // id
            Long id = teamQuery.getId();
            queryWrapper.eq(id != null && id >= 1, "id", id);
            // id 列表
            List<Long> idList = teamQuery.getIdList();
            queryWrapper.in(CollUtil.isNotEmpty(idList), "id", idList);
            // 搜索关键词
            String searchText = teamQuery.getSearchText();
            if (StrUtil.isNotBlank(searchText)) {
                queryWrapper.and(
                        qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 队伍名称
            String name = teamQuery.getName();
            queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
            // 最大人数
            Integer maxNum = teamQuery.getMaxNum();
            queryWrapper.eq(maxNum != null && maxNum > 0, "max_num", maxNum);
            // 创建人
            Long userId = teamQuery.getUserId();
            queryWrapper.eq(userId != null && userId > 0, "user_id", userId);
            // 状态
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                // 默认为公共
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }

        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(
                qw -> qw.gt("expire_time", new Date()).or().isNull("expire_time"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollUtil.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtil.copyProperties(team, teamUserVO);
            // 用户信息脱敏
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtil.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamInfo, User loginUser) {
        if (teamInfo == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long id = teamInfo.getId();
        if (id == null || id < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id参数错误");
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 只有管理员和队伍创建者可以修改队伍信息
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamInfo.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StrUtil.isBlank(teamInfo.getPassword())) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "加密房间需要设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtil.copyProperties(teamInfo, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinInfo, User loginUser) {
        if (teamJoinInfo == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long teamId = teamJoinInfo.getTeamId();
        if (teamId == null || teamId < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "teamId参数错误");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "不能加入私密队伍");
        }
        String password = teamJoinInfo.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StrUtil.isBlank(password)) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "加密房间需要密码才可加入");
            }
            if (!password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 该用户已加入的队伍数量
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("user_id", userId);
        long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (hasJoinNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多同时创建和加入5个队伍");
        }
        // todo 需要加上分布式锁，如果并发请求还是会同时加入
        // 不能重复加入已加入的队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("user_id", userId);
        userTeamQueryWrapper.eq("team_id", teamId);
        long hasUserJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (hasUserJoinNum > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
        }
        // 已加入队伍的人数
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        long teamHasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (teamHasJoinNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }
        // 加入，修改队伍信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }
}
