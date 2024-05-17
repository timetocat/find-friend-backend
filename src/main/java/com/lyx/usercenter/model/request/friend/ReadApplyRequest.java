package com.lyx.usercenter.model.request.friend;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 已读申请请求
 *
 * @author timecat
 * @create
 */
@Data
public class ReadApplyRequest implements Serializable {
    /**
     * 确定要设置已读申请的id集合
     */
    @ApiModelProperty("确定要设置已读申请的id集合")
    private Set<Long> ids;

    private static final long serialVersionUID = -5155165910553492221L;
}
