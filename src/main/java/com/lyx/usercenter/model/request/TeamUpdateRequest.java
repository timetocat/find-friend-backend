package com.lyx.usercenter.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户更新请求
 *
 * @author timecat
 * @create
 */
@Data
public class TeamUpdateRequest implements Serializable {

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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireTime;

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

    private static final long serialVersionUID = -3638576553979274705L;
}
