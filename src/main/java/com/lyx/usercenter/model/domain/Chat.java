package com.lyx.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 消息表
 * @TableName chat
 */
@TableName(value ="chat")
@Data
public class Chat implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 队伍id（群聊）
     */
    @TableField(value = "team_id")
    private Long teamId;

    /**
     * 发送者id
     */
    @TableField(value = "from_id")
    private Long fromId;

    /**
     * 接收者id（私聊）
     */
    @TableField(value = "to_id")
    private Long toId;

    /**
     * 内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 消息类(作用域)(0-私聊，1-群聊)
     */
    @TableField(value = "scope")
    private Integer scope;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 是否删除(0-未删除，1-删除)
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}