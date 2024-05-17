package com.lyx.usercenter.model.request.chat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author timecat
 * @create
 */
@Data
public class ChatRequest implements Serializable {
    /**
     * 队伍聊天室id
     */
    @ApiModelProperty("队伍聊天室id")
    private Long teamId;

    /**
     * 接收消息id
     */
    @ApiModelProperty("接收消息id")
    private Long toId;

    private static final long serialVersionUID = -6900227138160472443L;
}
