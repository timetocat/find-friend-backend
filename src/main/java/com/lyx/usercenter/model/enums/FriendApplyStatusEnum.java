package com.lyx.usercenter.model.enums;

import lombok.Getter;

/**
 * 好友申请状态枚举
 *
 * @author timecat
 * @create
 */
@Getter
public enum FriendApplyStatusEnum implements BaseIntEnum {

    NOT_DEAL(0, "未处理"),
    AGREE(1, "同意"),
    EXPIRED(2, "已过期"),
    CANCEL(3, "已撤销");

    private final int value;

    private final String text;

    FriendApplyStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }
}
