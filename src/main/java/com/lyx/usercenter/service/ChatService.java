package com.lyx.usercenter.service;

import com.lyx.usercenter.model.domain.Chat;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.enums.ChatScopeIntEnum;
import com.lyx.usercenter.model.request.chat.ChatRequest;
import com.lyx.usercenter.model.vo.MessageVO;

import java.util.Date;
import java.util.List;

/**
 * @author timecat
 * @description 针对表【chat(消息表)】的数据库操作Service
 * @createDate 2024-05-12 21:57:30
 */
public interface ChatService extends IService<Chat> {
    /**
     * 获取私聊信息
     *
     * @param chatRequest
     * @param loginUser
     * @return
     */
    List<MessageVO> getPrivateChat(ChatRequest chatRequest, User loginUser);

    /**
     * 设置结果集
     *
     * @param userId
     * @param toId
     * @param chat
     * @return
     */
    MessageVO chatResult(Long userId, Long toId, Chat chat);

    /**
     * 获取大厅信息
     *
     * @param chat
     * @return
     */
    MessageVO getHallMessage(Chat chat);

    /**
     * 获取大厅聊天信息
     *
     * @param loginUser
     * @return
     */
    List<MessageVO> getHallChat(User loginUser);

    /**
     * 获取队伍聊天信息
     *
     * @param teamId
     * @param loginUser
     * @return
     */
    List<MessageVO> getTeamChat(Long teamId, User loginUser);

    /**
     * 删除缓存
     *
     * @param key
     */
    void deleteKey(ChatScopeIntEnum chatScopeEnum, String key);
}
