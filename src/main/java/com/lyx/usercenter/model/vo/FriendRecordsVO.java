package com.lyx.usercenter.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author timecat
 * @create
 */
@Data
public class FriendRecordsVO implements Serializable {

    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;

    /**
     * 申请状态 默认0 （0-未通过 1-已同意 2-已过期）
     */
    @ApiModelProperty("申请状态 默认0 （0-未通过 1-已同意 2-已过期）")
    private Integer status;
    /**
     * 好友申请备注信息
     */
    @ApiModelProperty("好友申请备注信息")
    private String remark;
    /**
     * 申请用户信息
     */
    @ApiModelProperty("申请用户信息")
    private UserVO applyUser;

    private static final long serialVersionUID = -4459821600196745052L;
}
