package com.lyx.usercenter.model.request.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author timecat
 * @create
 */
@Data
public class SearchFriendsRequest implements Serializable {

    /**
     * 用户昵称
     */
    @ApiModelProperty("用户昵称")
    private String username;

    /**
     * 用户账号
     */
    @ApiModelProperty("用户账号")
    private String userAccount;
    private static final long serialVersionUID = -4548202570626698166L;
}
