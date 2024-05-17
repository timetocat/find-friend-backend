package com.lyx.usercenter.controller;

import com.lyx.usercenter.common.BaseResponse;
import com.lyx.usercenter.common.ErrorCode;
import com.lyx.usercenter.common.ResultUtils;
import com.lyx.usercenter.exception.BusinessException;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.request.chat.ChatRequest;
import com.lyx.usercenter.model.vo.MessageVO;
import com.lyx.usercenter.service.ChatService;
import com.lyx.usercenter.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author timecat
 * @create
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;
    @Resource
    private UserService userService;

    @PostMapping("/privateChat")
    public BaseResponse<List<MessageVO>> getPrivateChat
            (@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        if (chatRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求错误");
        }
        User loginUser = userService.getLoginUser(request);
        List<MessageVO> privateChatMessageList = chatService.getPrivateChat(chatRequest, loginUser);
        return ResultUtils.success(privateChatMessageList);
    }


}
