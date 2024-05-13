package com.lyx.usercenter.controller;

import com.lyx.usercenter.common.BaseResponse;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.common.IdRequest;
import com.lyx.usercenter.common.ResultUtils;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.request.friend.FriendAddRequest;
import com.lyx.usercenter.model.vo.FriendRecordsVO;
import com.lyx.usercenter.service.FriendsService;
import com.lyx.usercenter.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author timecat
 * @create
 */
@RestController
@RequestMapping("friends")
public class FriendsController {

    @Resource
    private FriendsService friendsService;
    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addFriendRecoder
            (@RequestBody FriendAddRequest friendAddRequest, HttpServletRequest request) {
        if (friendAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = friendsService.addFriendRecode(friendAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/getRecords")
    public BaseResponse<List<FriendRecordsVO>> getRecords(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<FriendRecordsVO> records = friendsService.getRecords(loginUser);
        return ResultUtils.success(records);
    }

    @GetMapping("/getNoReadRecordsCount")
    public BaseResponse<Integer> getRecordsCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        int count = friendsService.getNoReadRecordsCount(loginUser);
        return ResultUtils.success(count);
    }

    @GetMapping("/getMyApplyRecords")
    public BaseResponse<List<FriendRecordsVO>> getMyApplyRecords(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<FriendRecordsVO> records = friendsService.getMyApplyRecords(loginUser);
        return ResultUtils.success(records);
    }

    @GetMapping("/agree")
    public BaseResponse<Boolean> agree(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long fromId = idRequest.getId();
        boolean result = friendsService.agreeApply(fromId, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "同意添加好友失败");
        }
        return ResultUtils.success(true);
    }

}
