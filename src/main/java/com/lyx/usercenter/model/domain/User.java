package com.lyx.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户表
 *
 * @TableName user
 */
@TableName(value = "user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户昵称
     */
    @ApiModelProperty("用户昵称")
    @TableField(value = "username")
    private String username;

    /**
     * 用户账号
     */
    @ApiModelProperty("用户账号")
    @TableField(value = "user_account")
    private String userAccount;

    /**
     * 用户头像
     */
    @ApiModelProperty("用户头像")
    @TableField(value = "avatar_url")
    private String avatarUrl;

    /**
     * 性别
     */
    @ApiModelProperty("性别")
    @TableField(value = "gender")
    private Integer gender;

    /**
     * 个人简介
     */
    @ApiModelProperty("个人简介")
    @TableField(value = "profile")
    private String profile;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    @TableField(value = "user_password")
    private String userPassword;

    /**
     * 电话
     */
    @ApiModelProperty("电话")
    @TableField(value = "phone")
    private String phone;

    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    @TableField(value = "email")
    private String email;

    /**
     * 状态 0-正常
     */
    @ApiModelProperty("状态 0-正常")
    @TableField(value = "user_status")
    private Integer userStatus;

    /**
     * 用户角色 0-普通用户 1-管理员
     */
    @ApiModelProperty("用户角色 0-普通用户 1-管理员")
    @TableField(value = "user_role")
    private Integer userRole;

    /**
     * 标签 json 列表
     */
    @ApiModelProperty("标签 json 列表")
    @TableField(value = "tags")
    private String tags;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;

    /**
     * 是否删除 0-未删除 1-删除
     * 配置逻辑删除
     */
    @ApiModelProperty("是否删除 0-未删除 1-删除")
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @TableField(value = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}