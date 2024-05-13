package com.lyx.usercenter.model.request.team;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍请求
 *
 * @author timecat
 * @create
 */
@Data
public class TeamQuitRequest implements Serializable {
    /**
     * id
     */
    @ApiModelProperty("退出队伍id")
    private Long teamId;

    private static final long serialVersionUID = -1886604332064097055L;
}
