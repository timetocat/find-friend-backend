package com.lyx.usercenter.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author timecat
 * @create
 */
@Data
public class MessageVO implements Serializable {

    /**
     * 消息类(作用域)(0-私聊，1-群聊)
     */
    @ApiModelProperty("消息类(作用域)(0-私聊，1-群聊)")
    private Integer scope;

    /**
     * 内容
     */
    @ApiModelProperty("内容")
    private String content;

    /**
     * 队伍id（群聊）
     */
    @ApiModelProperty("队伍id（群聊）")
    private Long teamId;

    /**
     * 发送者
     */
    @ApiModelProperty("发送者")
    private ChatUserInfo fromUser;

    /**
     * 接收者（私聊）
     */
    @ApiModelProperty("接收者（私聊）")
    private ChatUserInfo toUser;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private String createTime;

    /**
     * 是否是我的消息
     */
    @ApiModelProperty("是否是我的消息")
    private Boolean isMy = false;

    /**
     * 是否是管理
     */
    @ApiModelProperty("是否是管理")
    private Boolean isAdmin = false;

    private static final long serialVersionUID = 8766116576183526038L;
}
