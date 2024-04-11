package com.lyx.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author timecat
 * @create 2023-12-19
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 345614971164090846L;

    String userAccount;
    String userPassword;
    String checkPassword;
}
