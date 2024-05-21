package com.lyx.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户包装类（脱敏）
 *
 * @author timecat
 * @create
 */
@Data
public class UserVO implements Serializable {
    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;

    /**
     * 用户账号
     */
    @ApiModelProperty("用户账号")
    @TableField(value = "user_account")
    private String userAccount;

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

    /**
     * 性别
     */
    @ApiModelProperty("性别")
    private Integer gender;

    /**
     * 个人简介
     */
    @ApiModelProperty("个人简介")
    private String profile;

    /**
     * 电话
     */
    @ApiModelProperty("电话")
    private String phone;

    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    private String email;

    /**
     * 用户角色 0-普通用户 1-管理员
     */
    @ApiModelProperty("用户角色 0-普通用户 1-管理员")
    private Integer userRole;

    /**
     * 标签 json 列表
     */
    @ApiModelProperty("标签 json 列表")
    private String tags;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @ApiModelProperty("创建时间")
    private Date createTime;

    private static final long serialVersionUID = -3180431606399608767L;
}
