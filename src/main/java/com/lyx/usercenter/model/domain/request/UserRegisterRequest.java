package com.lyx.usercenter.model.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author timecat
 * @create 2023-12-19
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 345614971164090846L;

    @ApiModelProperty(value = "用户账号", required = true)
    private String userAccount;

    @ApiModelProperty(value = "密码", required = true)
    private String userPassword;

    @ApiModelProperty(value = "确认密码", required = true)
    private String checkPassword;
}
