package com.lyx.usercenter.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyx.usercenter.common.BaseResponse;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.common.IdRequest;
import com.lyx.usercenter.common.ResultUtils;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.request.user.SearchFriendsRequest;
import com.lyx.usercenter.model.request.user.UpdateTagsRequest;
import com.lyx.usercenter.model.request.user.UserLoginRequest;
import com.lyx.usercenter.model.request.user.UserRegisterRequest;
import com.lyx.usercenter.model.vo.UserVO;
import com.lyx.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lyx.usercenter.constant.RedisKeys.INDEX_RECOMMEND;
import static com.lyx.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author timecat
 * @create 2023-12-19
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 注册用户
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验是否为空
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 再次校验
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "一项以上参数为空");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> current(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        // TODO 检验用户是否合法
        User user = userService.getById(userId);
        User safeUser = userService.getSafeUser(user);
        return ResultUtils.success(safeUser);
    }

    /**
     * 登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 校验是否为空
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 再次校验
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.useLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 登出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> logout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 根据对应用户
     *
     * @param username
     * @param request
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> search(String username, HttpServletRequest request) {
        // 仅限管理员可查询
        boolean isAdmin = userService.isAdmin(request);
        if (!isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream()
                .map(user -> userService.getSafeUser(user))
                .collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags
    (@RequestParam(required = false) List<String> tagNameList) {
        if (CollUtil.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @GetMapping("/userinfoVO")
    public BaseResponse<UserVO> getUserInfoVO(Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id为空或不符合要求");
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        return ResultUtils.success(userVO);
    }


    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommend(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format(INDEX_RECOMMEND + "%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接从缓存中取
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 存入缓存，10s过期

        try {
            valueOperations.set(redisKey, userPage, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("redis set error", e);
        }
        return ResultUtils.success(userPage);
    }

    /**
     * 更新个人信息
     *
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 1. 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "更新信息为空");
        }

        User loginUser = userService.getLoginUser(request);

        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 更新tags (标签)
     *
     * @param tagsRequest
     * @param request
     * @return
     */
    @PostMapping("/update/tags")
    public BaseResponse<Integer> updateTags(@RequestBody UpdateTagsRequest tagsRequest, HttpServletRequest request) {
        if (tagsRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Set<String> tags = tagsRequest.getTags();
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateTags(tags, loginUser);
        if (result != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(result);
    }

    /**
     * 注销用户（管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteById(@RequestBody long id, HttpServletRequest request) {
        // 仅管理员可删除
        boolean isAdmin = userService.isAdmin(request);
        if (!isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 匹配推荐用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("match")
    public BaseResponse<List<UserVO>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, loginUser));
    }

    /**
     * 获取用户好友
     *
     * @param request
     * @return
     */
    @GetMapping("/friends")
    public BaseResponse<List<UserVO>> getFriends(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getFriends(loginUser));
    }

    /**
     * 是否为好友
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/isFriend")
    public BaseResponse<Boolean> isFriend(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = userService.isFriend(loginUser.getId(), id);
        return ResultUtils.success(result);
    }

    /**
     * 刪除好友
     *
     * @param idRequest
     * @param request
     * @return
     */
    @DeleteMapping("/friends/delete")
    public BaseResponse<Boolean> deleteFriends(IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long friendId = idRequest.getId();
        boolean result = userService.removeFriend(friendId, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "刪除好友失敗");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/searchFriend")
    public BaseResponse<List<UserVO>> searchFriend
            (@RequestBody SearchFriendsRequest friendsRequest, HttpServletRequest request) {
        if (friendsRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<UserVO> friendList = userService.searchFriend(loginUser, friendsRequest);
        return ResultUtils.success(friendList);
    }


}
