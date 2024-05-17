package com.lyx.usercenter;

import cn.hutool.json.JSONUtil;
import com.lyx.usercenter.model.enums.ChatScopeIntEnum;
import com.lyx.usercenter.model.enums.BaseIntEnum;
import io.swagger.models.auth.In;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

//@SpringBootTest
class UserCenterApplicationTests {

    @Test
    void contextLoads() {

    }

    @Test
    void enumTest() {
        List<Integer> values = BaseIntEnum.getValues(ChatScopeIntEnum.class);
        System.out.println(values.toString());
        for (Integer value : values) {
            ChatScopeIntEnum enumByValue = BaseIntEnum.getEnumByValue(ChatScopeIntEnum.class, value);
            System.out.println(enumByValue);
        }
    }

}
