package com.lyx.usercenter.model.enums;

import cn.hutool.core.util.ObjectUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 枚举类公共方法
 *
 * @author timecat
 * @create
 */
public interface BaseIntEnum {
    /**
     * 获取value
     *
     * @return value
     */
    int getValue();

    /**
     * 获取text
     *
     * @return text
     */
    String getText();

    /**
     * 获取值列表
     *
     * @return
     */
    static <T extends Enum<T> & BaseIntEnum> List<Integer> getValues(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(BaseIntEnum::getValue)
                .collect(Collectors.toList());
    }


    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    static <T extends Enum<T> & BaseIntEnum> T getEnumByValue(Class<T> enumClass, Integer value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (T enumValue : enumClass.getEnumConstants()) {
            if (enumValue.getValue() == value) {
                return enumValue;
            }
        }
        return null;
    }
}
