package com.lyx.usercenter.utils;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * 字符串转换工具类
 *
 * @author timecat
 * @create
 */
public final class StrConvertUtils {

    private StrConvertUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Set<Long> stringToLongSet(String jsonList) {
        if (CharSequenceUtil.isBlank(jsonList)) {
            return new HashSet<>();
        }
        return JSONUtil.toBean(jsonList, new TypeReference<Set<Long>>() {
        }, false);
    }

    public static String longSetToString(Set<Long> set) {
        return JSONUtil.toJsonStr(set);
    }


    public static Set<String> stringToStringSet(String jsonList) {
        if (CharSequenceUtil.isBlank(jsonList)) {
            return new HashSet<>();
        }
        return JSONUtil.toBean(jsonList, new TypeReference<Set<String>>() {
        }, false);
    }

    public static String stringSetToString(Set<String> set) {
        return JSONUtil.toJsonStr(set);
    }

}
