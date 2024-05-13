package com.lyx.usercenter.model.enums;

import lombok.Getter;

/**
 * @author timecat
 * @create
 */
@Getter
public enum FriendIsReadIntEnum implements BaseIntEnum {

    READ(1, "已读"),

    UNREAD(0, "未读");

    private final int value;

    private final String text;

    FriendIsReadIntEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }
}

