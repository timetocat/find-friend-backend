package com.lyx.usercenter.model.enums;

import lombok.Getter;

/**
 * 聊天类型枚举
 *
 * @author timecat
 * @create
 */
@Getter
public enum ChatScopeIntEnum implements BaseIntEnum {

    PRIVATE_CHAT(0, "私聊"),
    TEAM_CHAT(1, "群聊"),
    HALL_CHAT(2, "大厅");

    private final int value;

    private final String text;

    ChatScopeIntEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

}
