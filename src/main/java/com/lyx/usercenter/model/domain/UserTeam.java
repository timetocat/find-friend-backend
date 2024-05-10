package com.lyx.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户队伍关系
 *
 * @TableName user_team
 */
@TableName(value = "user_team")
@Data
public class UserTeam implements Serializable {
    /**
     * id
     */
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    @ApiModelProperty("用户id")
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 队伍id
     */
    @ApiModelProperty("队伍id")
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 加入时间
     */
    @ApiModelProperty("加入时间")
    @TableField(value = "join_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date joinTime;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
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