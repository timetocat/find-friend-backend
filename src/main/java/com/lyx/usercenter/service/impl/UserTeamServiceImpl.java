package com.lyx.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyx.usercenter.model.domain.UserTeam;
import com.lyx.usercenter.service.UserTeamService;
import com.lyx.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author timecat
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-05-03 19:54:40
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




