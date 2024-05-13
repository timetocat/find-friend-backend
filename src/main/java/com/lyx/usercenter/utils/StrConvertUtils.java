package com.lyx.usercenter.utils;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;

import java.util.Set;

/**
 * 字符串转换工具类
 *
 * @author timecat
 * @create
 */
public final class StrConvertUtils {

    public static Set<Long> stringToLongSet(String jsonList) {
        return JSONUtil.toBean(jsonList, new TypeReference<Set<Long>>() {
        }, false);
    }

    public static String longSetToString(Set<Long> set) {
        return JSONUtil.toJsonStr(set);
    }
}
