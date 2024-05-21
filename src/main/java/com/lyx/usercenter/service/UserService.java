package com.lyx.usercenter.service;

import com.lyx.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyx.usercenter.model.request.user.SearchFriendsRequest;
import com.lyx.usercenter.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 用户服务
 *
 * @author timecat
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2023-12-18 17:04:46
 */
public interface UserService extends IService<User> {


    /**
     * @param userAccount   用户昵称
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * @param userAccount  用户昵称
     * @param userPassword 密码
     * @param request      cookie
     * @return 脱敏后的用户信息
     */
    User useLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * @param request 获取session
     * @return 返回一个标识
     */
    int userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 更新用户
     *
     * @param user
     * @param loginUser
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * @param user 源用户
     * @return 脱敏后的用户信息
     */
    User getSafeUser(User user);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    public boolean isAdmin(User loginUser);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 匹配推荐用户
     *
     * @param num
     * @param loginUser
     * @return
     */
    List<UserVO> matchUsers(long num, User loginUser);

    /**
     * 获取用户好友
     *
     * @param currentUser
     * @return
     */
    List<UserVO> getFriends(User currentUser);

    /**
     * 更新标签
     *
     * @param tags
     * @param currentUser
     * @return
     */
    Integer updateTags(Set<String> tags, User currentUser);

    /**
     * 刪除好友
     *
     * @param friendId
     * @param loginUser
     * @return
     */
    boolean removeFriend(long friendId, User loginUser);

    /**
     * 判断是否是好友
     *
     * @param loginUserId
     * @param friendId
     * @return
     */
    boolean isFriend(long loginUserId, long friendId);

    /**
     * 搜索好友
     *
     * @param loginUser
     * @param friendsRequest
     * @return
     */
    List<UserVO> searchFriend(User loginUser, SearchFriendsRequest friendsRequest);
}
