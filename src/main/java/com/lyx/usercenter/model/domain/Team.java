package com.lyx.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 队伍
 *
 * @TableName team
 */
@TableName(value = "team")
@Data
public class Team implements Serializable {
    /**
     * id
     */
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 队伍名称
     */
    @ApiModelProperty("队伍名称")
    @TableField(value = "name")
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    @TableField(value = "description")
    private String description;

    /**
     * 最大人数
     */
    @ApiModelProperty("最大人数")
    @TableField(value = "max_num")
    private Integer maxNum;

    /**
     * 过期时间
     */
    @ApiModelProperty("过期时间")
    @TableField(value = "expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 用户id（队长 id）
     */
    @ApiModelProperty("用户id（队长 id）")
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @ApiModelProperty("0 - 公开，1 - 私有，2 - 加密")
    @TableField(value = "status")
    private Integer status;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    @TableField(value = "password")
    private String password;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @TableField(value = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 是否删除
     */
    @ApiModelProperty("是否删除")
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}