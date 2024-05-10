package com.lyx.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyx.usercenter.model.domain.UserTeam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author timecat
 * @description 针对表【user_team(用户队伍关系)】的数据库操作Mapper
 * @createDate 2024-05-03 19:54:40
 * @Entity com.lyx.usercenter.model.domain.UserTeam
 */
public interface UserTeamMapper extends BaseMapper<UserTeam> {

    /**
     * 查询曾经是否加入过队伍
     */
    List<Long> getDeleteDate(@Param("user_id") long userId, @Param("team_id") long teamId);

    int updateDeleteByJoin(@Param("user_id") long userId, @Param("team_id") long teamId);
}




