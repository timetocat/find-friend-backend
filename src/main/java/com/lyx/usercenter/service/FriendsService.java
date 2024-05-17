package com.lyx.usercenter.service;

import com.lyx.usercenter.common.IdRequest;
import com.lyx.usercenter.model.domain.Friends;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.request.friend.FriendAddRequest;
import com.lyx.usercenter.model.vo.FriendRecordsVO;

import java.util.List;
import java.util.Set;

/**
 * @author timecat
 * @description 针对表【friends(好友申请管理表)】的数据库操作Service
 * @createDate 2024-05-12 21:57:30
 */
public interface FriendsService extends IService<Friends> {

    /**
     * 申请好友
     *
     * @param friendAddRequest
     * @param loginUser
     * @return
     */
    boolean addFriendRecode(FriendAddRequest friendAddRequest, User loginUser);

    /**
     * 获取申请记录（同意、申请的）
     *
     * @param loginUser
     * @return
     */
    List<FriendRecordsVO> getRecords(User loginUser);

    /**
     * 获取未读记录
     *
     * @param loginUser
     * @return
     */
    int getNoReadRecordsCount(User loginUser);

    /**
     * 获取我的申请记录
     *
     * @param loginUser
     * @return
     */
    List<FriendRecordsVO> getMyApplyRecords(User loginUser);

    /**
     * 同意好友申请
     *
     * @param fromId
     * @param loginUser
     * @return
     */
    boolean agreeApply(long fromId, User loginUser);

    /**
     * 撤销我的申请
     *
     * @param id
     * @param loginUser
     * @return
     */
    boolean cancelApply(long id, User loginUser);

    /**
     * 设置申请为已读
     *
     * @param ids
     * @param loginUser
     * @return
     */
    boolean toRead(Set<Long> ids, User loginUser);
}
