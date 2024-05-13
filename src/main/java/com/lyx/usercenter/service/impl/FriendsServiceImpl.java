package com.lyx.usercenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.mapper.FriendsMapper;
import com.lyx.usercenter.model.domain.Friends;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.domain.UserFriends;
import com.lyx.usercenter.model.request.friend.FriendAddRequest;
import com.lyx.usercenter.model.vo.FriendRecordsVO;
import com.lyx.usercenter.model.vo.UserVO;
import com.lyx.usercenter.service.FriendsService;
import com.lyx.usercenter.service.UserFriendsService;
import com.lyx.usercenter.service.UserService;
import com.lyx.usercenter.utils.StrConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lyx.usercenter.constant.FriendConstant.FROM_ROLE;
import static com.lyx.usercenter.constant.FriendConstant.RECEIVE_ROLES;
import static com.lyx.usercenter.constant.RedisKeys.ADD_FRIEND;
import static com.lyx.usercenter.model.enums.FriendApplyStatusEnum.*;
import static com.lyx.usercenter.model.enums.FriendIsReadIntEnum.READ;
import static com.lyx.usercenter.model.enums.FriendIsReadIntEnum.UNREAD;

/**
 * @author timecat
 * @description 针对表【friends(好友申请管理表)】的数据库操作Service实现
 * @createDate 2024-05-12 21:57:30
 */
@Slf4j
@Service
public class FriendsServiceImpl extends ServiceImpl<FriendsMapper, Friends>
        implements FriendsService {

    @Resource
    private UserService userService;
    @Resource
    private UserFriendsService userFriendsService;
    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean addFriendRecode(FriendAddRequest friendAddRequest, User loginUser) {
        String remark = friendAddRequest.getRemark();
        if (StrUtil.isNotBlank(remark) && remark.length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申请备注过长");
        }
        Long userId = loginUser.getId();
        Long receiveId = friendAddRequest.getReceiveId();
        if (ObjectUtils.anyNull(userId, receiveId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (userId.equals(receiveId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能添加自己为好友");
        }
        String redisKey = String.format(ADD_FRIEND + "%s", userId);
        RLock lock = redissonClient.getLock(redisKey);
        try {
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
                log.info("getLock ADD_FRIEND: " + Thread.currentThread().getId());
                QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
                friendsQueryWrapper.eq("from_id", userId)
                        .eq("receive_id", receiveId);
                List<Friends> friendList = this.list(friendsQueryWrapper);
                friendList.forEach(friends -> {
                    // 对方还未处理不能再次申请
                    if (friendList.size() > 1 && friends.getStatus() == NOT_DEAL.getValue()) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不能重复申请");
                    }
                });
                Friends newFriend = new Friends();
                newFriend.setFromId(userId);
                newFriend.setReceiveId(receiveId);
                if (StrUtil.isBlank(remark)) {
                    newFriend.setRemark("你好！我是" + userService.getById(userId).getUsername());
                } else {
                    newFriend.setRemark(remark);
                }
                return this.save(newFriend);
            }
        } catch (InterruptedException e) {
            log.error("添加好友，获取锁失败", e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("unLock ADD_FRIEND: " + Thread.currentThread().getId());
            }
        }
        return false;
    }

    @Override
    public List<FriendRecordsVO> getRecords(User loginUser) {
        // 查询当前用户申请记录（同意以及申请的）
        QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
        friendsQueryWrapper.eq("receive_id", loginUser.getId())
                .orderByDesc("create_time");
        List<Friends> friendList = this.list(friendsQueryWrapper);
        return getFriendRecordsVOList(friendList, FROM_ROLE);
    }

    @Override
    public int getNoReadRecordsCount(User loginUser) {
        QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
        friendsQueryWrapper.eq("receive_id", loginUser.getId())
                .eq("is_read", UNREAD.getValue())
                .eq("status", NOT_DEAL.getValue()); // 过期就不显示了
        return (int) this.count(friendsQueryWrapper);
    }

    @Override
    public List<FriendRecordsVO> getMyApplyRecords(User loginUser) {
        // 查询当前用户所申请、同意的记录
        QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
        friendsQueryWrapper.eq("from_id", loginUser.getId())
                .ne("status", EXPIRED.getValue())
                .orderByDesc("create_time");
        List<Friends> friendList = this.list(friendsQueryWrapper);
        return getFriendRecordsVOList(friendList, RECEIVE_ROLES);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean agreeApply(long fromId, User loginUser) {
        Long receiveId = loginUser.getId();
        if (receiveId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询未过期的申请记录
        QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
        friendsQueryWrapper.eq("from_id", fromId)
                .eq("receive_id", receiveId)
                .eq("status", NOT_DEAL.getValue());
        List<Friends> friendList = this.list(friendsQueryWrapper);
        if (CollUtil.isEmpty(friendList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申请不存在");
        }
        if (friendList.size() > 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "操作出错，请重试");
        }
        AtomicBoolean flag = new AtomicBoolean(false);
        friendList.forEach(friend -> {
            // 修改用户好友表
            UserFriends fromUserFriends = getFriendIds(fromId);
            UserFriends receiveUserFriends = getFriendIds(receiveId);
            if (fromUserFriends == null || receiveUserFriends == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户异常");
            }
            // 添加新朋友
            String newFromUserFriendIds = addFriendId(fromUserFriends.getFriendIds(), receiveId);
            String newReceiveUserFriendIds = addFriendId(receiveUserFriends.getFriendIds(), fromId);
            fromUserFriends.setFriendIds(newFromUserFriendIds);
            receiveUserFriends.setFriendIds(newReceiveUserFriendIds);

            // 设置申请同意状态和已读状态
            friend.setStatus(AGREE.getValue());
            friend.setIsRead(READ.getValue());
            flag.set(this.updateById(friend)
                    && userFriendsService.updateById(fromUserFriends)
                    && userFriendsService.updateById(receiveUserFriends));
        });
        return flag.get();
    }

    private String addFriendId(String ids, long addId) {
        Set<Long> idsSet = StrConvertUtils.stringToLongSet(ids);
        idsSet.add(addId);
        return StrConvertUtils.longSetToString(idsSet);
    }

    private List<FriendRecordsVO> getFriendRecordsVOList(List<Friends> friendList, String role) {
        if (CollUtil.isEmpty(friendList)) {
            return Collections.emptyList();
        }
        // 判断是申请者还是接收者（在好友申请表中）
        Function<Friends, User> userGetter = RECEIVE_ROLES.equals(role) ?
                friend -> userService.getById(friend.getReceiveId()) :
                friend -> userService.getById(friend.getFromId());

        return friendList.stream().map(friend -> {
            FriendRecordsVO friendRecordsVO = BeanUtil
                    .copyProperties(friend, FriendRecordsVO.class);
            User user = userGetter.apply(friend);
            friendRecordsVO.setApplyUser(BeanUtil
                    .copyProperties(user, UserVO.class));
            return friendRecordsVO;
        }).collect(Collectors.toList());
    }


    private UserFriends getFriendIds(long userId) {
        QueryWrapper<UserFriends> userFriendsQueryWrapper = new QueryWrapper<>();
        userFriendsQueryWrapper.eq("user_id", userId);
        return userFriendsService.getOne(userFriendsQueryWrapper);
    }
}




