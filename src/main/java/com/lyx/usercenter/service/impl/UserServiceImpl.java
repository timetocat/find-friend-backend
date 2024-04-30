package com.lyx.usercenter.service.impl;

import java.util.*;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.model.User;
import com.lyx.usercenter.service.UserService;
import com.lyx.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lyx.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.lyx.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author timecat
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2023-12-18 17:04:46
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 加盐，盐值，混淆密码
     */
    private static final String SALT = "lyx";
    @Resource
    private UserMapper userMapper;

    /**
     * @param userAccount   用户昵称
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1. 校验
        // 必填选项是否输出
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "一项以上参数为空");
        }
        // 用户昵称不得小于四位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户昵称小于四位");
        }
        // 用户密码不得小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码小于8位");
        }

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户包含特殊字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验密码不一致");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户重复");
        }

        //2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    /**
     * @param userAccount  用户昵称
     * @param userPassword 密码
     * @param request      cookie
     * @return 脱敏后的用户信息
     */
    @Override
    public User useLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        // 检查用户昵称和密码是否为空
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户昵称或者密码为空");
        }
        // 用户昵称和密码位数要求,用户昵称不得小于4位，用户密码不得小于8位
        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户昵称和密码不符合要求");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\]" +
                ".<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户包含特殊字符");
        }

        // 2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex
                ((SALT + userPassword).getBytes());
        // 查询用户是否存在
        // 需要开启逻辑删除，不然被逻辑删除的用户也会被查找出来
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount Cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户或密码不正确");
        }

        // 3.脱敏
        User safeUser = getSafeUser(user);

        // 4.记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safeUser);

        return safeUser;
    }

    /**
     * @param request 获取session
     * @return 返回一个标识
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录状态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return user;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        if (user == null || loginUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long userId = user.getId();
        if (userId < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!checkUpdateFiled(user)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "更新字段为空");
        }
        // 如果是管理员，允许更新任意用户信息
        // todo 管理员应该不能更新其他管理员账户
        // 如果是自己，只允许更新自己的信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        return userMapper.updateById(user);
    }

    private boolean checkUpdateFiled(User user) {
        return Stream.of(
                user.getUsername(),
                user.getAvatarUrl(),
                user.getGender(),
                user.getProfile(),
                user.getPhone(),
                user.getEmail(),
                user.getTags()
        ).anyMatch(Objects::nonNull);
    }

    /**
     * 用户脱敏
     *
     * @param user 原信息
     * @return 安全用户
     */
    @Override
    public User getSafeUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息错误");
        }
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setGender(user.getGender());
        safeUser.setProfile(user.getProfile());
        safeUser.setTags(user.getTags());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setCreateTime(user.getCreateTime());
        return safeUser;
    }

    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        // 1. 判空
        if (CollUtil.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 2. 先查询所有用户
        List<User> userList = userMapper.selectList(queryWrapper);

        Gson gson = new Gson();
        // 3. 判断内存中是否包含要求的标签
        return userList.stream().filter(user -> {
            String tags = user.getTags();
            Set<String> tempTagsSet = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            tempTagsSet = Optional.ofNullable(tempTagsSet)
                    .orElse(new HashSet<>());
            for (String tag : tagNameList) {
                if (!tempTagsSet.contains(tag)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafeUser).collect(Collectors.toList());

    }

    /**
     * 根据标签搜索用户（sql查询版）
     *
     * @param tagNameList
     * @return
     * @deprecated 过时
     */
    @Deprecated
    private List<User> searchUsersByTagBySQL(List<String> tagNameList) {
        // 1. 判空
        if (CollUtil.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 2. 拼接sql
        // like '%Java%' and like '%Python%'
        for (String tags : tagNameList) {
            queryWrapper.like("tags", tags);
        }
        // 3. 进行查询
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafeUser)
                .collect(Collectors.toList());
    }


    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    private static boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return !(user == null || user.getUserRole() != ADMIN_ROLE);
    }

    /**
     * 是否是管理员
     *
     * @param loginUser
     * @return
     */
    private static boolean isAdmin(User loginUser) {
        return !(loginUser == null || loginUser.getUserRole() != ADMIN_ROLE);
    }
}




