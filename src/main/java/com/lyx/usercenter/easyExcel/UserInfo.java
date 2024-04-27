package com.lyx.usercenter.easyExcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用户信息
 *
 * @author timecat
 * @create
 */
@Data
public class UserInfo {

    /**
     * userAccount
     */
    @ExcelProperty("用户账户")
    private String userAccount;
    /**
     * username
     */
    @ExcelProperty("用户昵称")
    private String username;
}
