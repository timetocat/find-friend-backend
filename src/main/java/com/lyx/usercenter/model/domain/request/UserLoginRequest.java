package com.lyx.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author timecat
 * @create
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 4619562148257145703L;

    String userAccount;
    String userPassword;
}
