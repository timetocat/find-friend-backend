package com.lyx.usercenter.model.request.friend;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 好友申请请求
 *
 * @author timecat
 * @create
 */
@Data
public class FriendAddRequest implements Serializable {

    /**
     * 接收申请的用户id
     */
    @ApiModelProperty("接收申请的用户id")
    private Long receiveId;

    /**
     * 好友申请备注信息
     */
    @ApiModelProperty("好友申请备注信息")
    private String remark;

    private static final long serialVersionUID = -4152929430478225513L;
}
