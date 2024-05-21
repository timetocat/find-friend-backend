package com.lyx.usercenter.model.request.chat;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息请求类
 *
 * @author timecat
 * @create
 */
@Data
public class MessageRequest implements Serializable {

    /**
     * 队伍id（群聊）
     */
    private Long teamId;

    /**
     * 接收者id（私聊）
     */
    private Long toId;

    /**
     * 内容
     */
    private String content;

    /**
     * 消息类(作用域)(0-私聊，1-群聊)
     */
    private Integer scope;

    /**
     * 是否是管理员
     */
    private boolean isAdmin;

    private static final long serialVersionUID = -479884084996595136L;
}
