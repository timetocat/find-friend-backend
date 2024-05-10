package com.lyx.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.model.domain.UserTeam;
import com.lyx.usercenter.service.UserTeamService;
import com.lyx.usercenter.mapper.UserTeamMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author timecat
 * @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
 * @createDate 2024-05-03 19:54:40
 */
@Service
@Slf4j
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
        implements UserTeamService {

    @Resource
    UserTeamMapper userTeamMapper;

    @Override
    public List<Long> getBeforeDate(long userId, long teamId) {
        return userTeamMapper.getDeleteDate(userId, teamId);
    }

    @Override
    public boolean updateDeleteByJoin(long userId, long teamId) {
        try {
            int result = userTeamMapper.updateDeleteByJoin(userId, teamId);
            return result == 1;
        } catch (Exception e) {
            log.error("系统错误，加入曾经加入过的队伍，更新逻辑删除字段");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }
}




