package com.lyx.usercenter.model.request.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author timecat
 * @create
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 4619562148257145703L;

    @ApiModelProperty(value = "用户账号", required = true)
    private String userAccount;

    @ApiModelProperty(value = "密码", required = true)
    private String userPassword;
}
