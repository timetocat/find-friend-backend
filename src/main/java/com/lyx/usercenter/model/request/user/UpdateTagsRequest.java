package com.lyx.usercenter.model.request.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 修改标签请求
 *
 * @author timecat
 * @create
 */
@Data
public class UpdateTagsRequest implements Serializable {

    /**
     * 修改后的标签内容
     */
    @ApiModelProperty("修改后的标签内容")
    private Set<String> tags;

    private static final long serialVersionUID = -5666848042406120338L;
}
