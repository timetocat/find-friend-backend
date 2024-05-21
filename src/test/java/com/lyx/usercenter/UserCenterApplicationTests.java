package com.lyx.usercenter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.model.enums.ChatScopeIntEnum;
import com.lyx.usercenter.model.enums.BaseIntEnum;
import com.lyx.usercenter.model.vo.ChatUserInfo;
import io.swagger.models.auth.In;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
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

    @Test
    void dateTest() {
        String resource = DateUtil.format(DateTime.now()
                , "yyyy年MM月dd日 HH:mm:ss");
        System.out.println(resource);
        Date dateTime = DateUtil.parse(resource, "yyyy年MM月dd日 HH:mm:ss");
        System.out.println(dateTime);
    }

    @Test
    void copyByAliasTest() {
        User user = new User();
        user.setId(1L);
        ChatUserInfo userInfo = BeanUtil.copyProperties(user, ChatUserInfo.class);
        Assertions.assertNotNull(userInfo.getUserId());
        System.out.println(userInfo.getUserId());
    }
}
