package com.lyx.usercenter.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author timecat
 * @create
 */
@Data
public class IdRequest implements Serializable {
    /**
     * id
     */
    @ApiModelProperty(value = "id", required = true)
    private long id;

    private static final long serialVersionUID = 243350674279923545L;
}
