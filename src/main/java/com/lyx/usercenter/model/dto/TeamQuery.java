package com.lyx.usercenter.model.dto;

import com.lyx.usercenter.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 队伍查询封装类
 *
 * @author timecat
 * @create
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamQuery extends PageRequest {
    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;
    /**
     * id 列表
     */
    @ApiModelProperty("id 列表")
    private List<Long> idList;
    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    @ApiModelProperty("搜索关键词（同时对队伍名称和描述搜索）")
    private String searchText;
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
     * 用户id
     */
    @ApiModelProperty("用户id")
    private Long userId;
    /**
     * 队伍状态（0-公开，1-私有，2-加密）
     */
    @ApiModelProperty("队伍状态（0-公开，1-私有，2-加密）")
    private Integer status;

    private static final long serialVersionUID = -7978419019528715133L;
}
