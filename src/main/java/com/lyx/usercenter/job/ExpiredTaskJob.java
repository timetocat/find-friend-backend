package com.lyx.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.model.domain.Team;
import com.lyx.usercenter.service.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 定期解散队伍（每天凌晨零点）
 *
 * @author timecat
 * @create
 */
@Slf4j
@Component
public class ExpiredTaskJob {

    @Resource
    private TeamService teamService;

    // 每天凌晨零点执行，解散过期队伍
    @Scheduled(cron = "0 0 0 * * ?")
    public void expiredTeamDisbandTask() {
        // 找到过期的队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("expire_time", new Date());
        List<Team> teamList = teamService.list(queryWrapper);
        teamList.forEach(team -> {
            Long teamId = team.getId();
            boolean result = teamService.disbandTeam(teamId);
            if (!result) {
                log.error("解散队伍失败，teamId={}", teamId);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散队伍失败");
            } else {
                log.info("解散队伍成功，teamId={}", teamId);
            }
        });
    }
}
