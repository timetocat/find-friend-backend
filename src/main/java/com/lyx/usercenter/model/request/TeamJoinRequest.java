package com.lyx.usercenter.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入队伍请求
 *
 * @author timecat
 * @create
 */
@Data
public class TeamJoinRequest implements Serializable {

    /**
     * id
     */
    @ApiModelProperty("队伍id")
    private Long teamId;
    /**
     * 密码
     */
    @ApiModelProperty(required = false, value = "密码")
    private String password;

    private static final long serialVersionUID = 5454787484456417497L;
}
