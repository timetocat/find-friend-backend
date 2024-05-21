package com.lyx.usercenter.model.vo;

import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author timecat
 * @create
 */
@Data
public class ChatUserInfo implements Serializable {
    /**
     * userId
     */
    @Alias("id")
    @ApiModelProperty("用户id")
    private Long userId;

    /**
     * 用户昵称
     */
    @ApiModelProperty("用户昵称")
    private String username;

    /**
     * 用户头像
     */
    @ApiModelProperty("用户头像")
    private String avatarUrl;

    private static final long serialVersionUID = 8337846670778637482L;
}
