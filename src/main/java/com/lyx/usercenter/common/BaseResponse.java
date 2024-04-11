package com.lyx.usercenter.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 返回通用类
 *
 * @param <T>
 * @author timecat
 * @create
 */
@Data
@AllArgsConstructor
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 538891144916247585L;

    private int code;
    private T data;
    private String message;
    private String description;

    public BaseResponse(int code, T data) {
        this(code, data, "","");
    }
    public BaseResponse(int code, T data,String message) {
        this(code, data, message,"");
    }
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }
}
