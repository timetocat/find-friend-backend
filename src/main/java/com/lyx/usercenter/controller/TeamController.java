package com.lyx.usercenter.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyx.usercenter.common.BaseResponse;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.common.ResultUtils;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.model.domain.Team;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.dto.TeamQuery;
import com.lyx.usercenter.model.request.TeamAddRequest;
import com.lyx.usercenter.model.request.TeamJoinRequest;
import com.lyx.usercenter.model.request.TeamUpdateRequest;
import com.lyx.usercenter.model.vo.TeamUserVO;
import com.lyx.usercenter.service.TeamService;
import com.lyx.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author timecat
 * @create
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtil.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestParam long id, HttpServletRequest request) {
        if (id < 1) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "id不正确");
        }
        // todo 创建人或者管理员才可以删除队伍
        boolean result = teamService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam
            (@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long id) {
        if (id < 1) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "id不正确");
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // todo 信息脱敏
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeam(teamQuery, isAdmin);
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listPageTeam(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int pageNum = teamQuery.getPageNum();
        int pageSize = teamQuery.getPageSize();
        // todo 逻辑处理
        Team team = new Team();
        BeanUtil.copyProperties(teamQuery, team);
        Page<Team> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        // todo 信息脱敏
        return ResultUtils.success(teamPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

}
