package com.lyx.usercenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.mapper.UserMapper;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.domain.UserFriends;
import com.lyx.usercenter.model.vo.UserVO;
import com.lyx.usercenter.service.UserFriendsService;
import com.lyx.usercenter.service.UserService;
import com.lyx.usercenter.utils.AlgorithmUtils;
import com.lyx.usercenter.utils.StrConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
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

    @Resource
    private UserFriendsService userFriendsService;

    /**
     * @param userAccount   用户昵称
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Transactional(rollbackFor = Exception.class)
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
        Long userId = user.getId();
        UserFriends userFriends = new UserFriends();
        userFriends.setUserId(userId);
        boolean save = userFriendsService.save(userFriends);
        if (!save) {
            return -1;
        }
        return userId;
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
            throw new BusinessException(ErrorCode.NO_AUTH, "未登录");
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
        if (!this.isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        return userMapper.updateById(user);
    }

    /**
     * 校验是否有更新参数
     *
     * @param user
     * @return
     */
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

        // 3. 判断内存中是否包含要求的标签
        return userList.stream().filter(user -> {
            String tags = user.getTags();
//            Set<String> tempTagsSet = Convert.convert(new TypeReference<Set<String>>() {
//            }, CharSequenceUtil.removeAll(tags, "\""));
            Set<String> tempTagsSet = JSONUtil.toBean(tags, new TypeReference<Set<String>>() {
            }, false);
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

    @Override
    public List<UserVO> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags")
                .select("id", "tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        List<String> tagList = JSONUtil.toList(tags, String.class);

        // 用户列表下标(userList) => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            // 无标签过滤
            if (StrUtil.isBlank(userTags)) {
                continue;
            }
            // 自己除外
            if (Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagsList = JSONUtil.toList(userTags, String.class);
            // 计算相似度
            long distanceScore = AlgorithmUtils.minDistance(tagList, userTagsList);
            list.add(new Pair<>(user, distanceScore));
        }
        List<Pair<User, Long>> toUserPairList = list.stream().sorted(
                        (o1, o2) -> (int) (o1.getValue() - o2.getValue()))
                .limit(num).collect(Collectors.toList());
        // 有序userID表
        List<Long> userVOId = toUserPairList.stream().map(
                        pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        // 根据id查询user信息
        queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", userVOId);
        Map<Long, List<UserVO>> userIdUserListMap = this.list(queryWrapper).stream().map(
                user -> BeanUtil.toBean(user, UserVO.class)
        ).collect(Collectors.groupingBy(UserVO::getId));
        // 因为map乱序，需根据有序userId赋值
        List<UserVO> userVOList = new ArrayList<>();
        for (Long userId : userVOId) {
            userVOList.add(userIdUserListMap.get(userId).get(0));
        }
        return userVOList;
    }

    @Override
    public List<UserVO> getFriends(User currentUser) {
        Long userId = currentUser.getId();
        QueryWrapper<UserFriends> userFriendsQueryWrapper = new QueryWrapper<>();
        userFriendsQueryWrapper.eq("user_id", userId);
        UserFriends userFriends = userFriendsService.getOne(userFriendsQueryWrapper);
        if (userFriends == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Set<Long> friendIdSet = StrConvertUtils.stringToLongSet(userFriends.getFriendIds());
        if (CollUtil.isEmpty(friendIdSet)) {
            return Collections.emptyList();
        }
        return friendIdSet.stream().map(friendId ->
                        BeanUtil.toBean(this.getById(friendId), UserVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Integer updateTags(Set<String> tags, User currentUser) {
        Long userId = currentUser.getId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户不存在");
        }
        String newTags = StrConvertUtils.stringSetToString(tags);
        user.setTags(newTags);
        return userMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeFriend(long friendId, User loginUser) {
        Long userId = loginUser.getId();
        UserFriends userFriends = this.getUserFriends(userId);
        UserFriends friendUserFriends = this.getUserFriends(friendId);
        // 判断好友关系是否存在
        if (userFriends == null || friendUserFriends == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您不是好友");
        }
        userFriends.setFriendIds(removeFriendId(userFriends.getFriendIds(), friendId));
        friendUserFriends.setFriendIds(removeFriendId(friendUserFriends.getFriendIds(), userId));
        return (userFriendsService.updateById(userFriends)
                && userFriendsService.updateById(friendUserFriends));
    }

    private UserFriends getUserFriends(long userId) {
        QueryWrapper<UserFriends> userFriendsQueryWrapper = new QueryWrapper<>();
        userFriendsQueryWrapper.eq("user_id", userId);
        return userFriendsService.getOne(userFriendsQueryWrapper);
    }

    private String removeFriendId(String ids, long removeId) {
        Set<Long> friendIds = StrConvertUtils.stringToLongSet(ids);
        friendIds.removeIf(id -> id == removeId);
        return StrConvertUtils.longSetToString(friendIds);
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
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return !(user == null || user.getUserRole() != ADMIN_ROLE);
    }

    /**
     * 是否是管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return !(loginUser == null || loginUser.getUserRole() != ADMIN_ROLE);
    }
}




