package com.lyx.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求
 *
 * @author timecat
 * @create
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -4728631475998039493L;

    /**
     * 页面大小
     */
    protected int pageSize = 10;
    /**
     * 当前页号
     */
    protected int pageNum = 1;
}
