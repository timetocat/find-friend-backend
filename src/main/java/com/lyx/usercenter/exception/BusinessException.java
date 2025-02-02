package com.lyx.usercenter.exception;

import com.lyx.usercenter.common.ErrorCode;

/**
 * 自定义异常类
 * @author timecat
 * @create
 */
public class BusinessException extends RuntimeException{

    private static final long serialVersionUID = -7107548065845345098L;
    private final int code;
    private final String description;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode,String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
}
