package com.lyx.usercenter.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户和队伍信息包装类（脱敏）
 *
 * @author timecat
 * @create
 */
@Data
public class TeamUserVO implements Serializable {
    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 用户id
     */
    @ApiModelProperty("用户id")
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @ApiModelProperty("0 - 公开，1 - 私有，2 - 加密")
    private Integer status;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @ApiModelProperty("创建时间")
    private Date createTime;

    /**
     * 创建人信息
     */
    @ApiModelProperty("创建人信息")
    private UserVO createUser;

    /**
     * 已加入的用户数
     */
    @ApiModelProperty("已加入的用户数")
    private Integer hasJoinNum;

    /**
     * 是否已加入队伍
     */
    @ApiModelProperty("是否已加入队伍")
    private Boolean hasJoin = Boolean.FALSE;

    private static final long serialVersionUID = -8261549855917473415L;
}
