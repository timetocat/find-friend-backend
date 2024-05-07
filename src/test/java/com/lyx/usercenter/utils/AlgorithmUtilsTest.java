package com.lyx.usercenter.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * 算法工具测试类
 *
 * @author timecat
 * @create
 */
class AlgorithmUtilsTest {

    @Test
    void test() {
        List<String> tagList1 = Arrays.asList("Java", "男", "大一");
        List<String> tagList2 = Arrays.asList("Java", "女", "大一");
        List<String> tagList3 = Arrays.asList("Python", "女", "大二");
        // 通过测试
        int score1 = AlgorithmUtils.minDistance(tagList1, tagList2);
        int expectedScore1 = 1;
        Assertions.assertEquals(expectedScore1, score1);
        int score2 = AlgorithmUtils.minDistance(tagList1, tagList3);
        int expectedScore2 = 3;
        Assertions.assertEquals(expectedScore2, score2);
    }
}