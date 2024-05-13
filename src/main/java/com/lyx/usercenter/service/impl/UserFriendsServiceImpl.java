package com.lyx.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyx.usercenter.model.domain.UserFriends;
import com.lyx.usercenter.service.UserFriendsService;
import com.lyx.usercenter.mapper.UserFriendsMapper;
import org.springframework.stereotype.Service;

/**
* @author timecat
* @description 针对表【user_friends(用户好友表)】的数据库操作Service实现
* @createDate 2024-05-13 20:40:00
*/
@Service
public class UserFriendsServiceImpl extends ServiceImpl<UserFriendsMapper, UserFriends>
    implements UserFriendsService{

}




