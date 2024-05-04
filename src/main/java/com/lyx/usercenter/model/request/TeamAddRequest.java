package com.lyx.usercenter.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author timecat
 * @create
 */
@Data
public class TeamAddRequest implements Serializable {
    /**
     * 队伍名称
     */
    @ApiModelProperty("队伍名称")
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String description;

    /**
     * 最大人数
     */
    @ApiModelProperty("最大人数")
    private Integer maxNum;

    /**
     * 过期时间
     */
    @ApiModelProperty("过期时间")
    private Date expireTime;

    /**
     * 用户id（队长 id）
     */
    @ApiModelProperty("用户id（队长 id）")
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @ApiModelProperty("0 - 公开，1 - 私有，2 - 加密")
    private Integer status;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    private String password;

    private static final long serialVersionUID = 3214364098574920835L;
}
