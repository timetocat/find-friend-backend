package com.lyx.usercenter.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;

/**
 * @author timecat
 * @create
 */
@SpringBootTest
class UserTeamServiceTest {

    @Resource
    UserTeamService userTeamService;

    @Test
    void getHasBeforeDate() {
        List<Long> beforeDate = userTeamService.getBeforeDate(2, 1788464831712747521L);
        Assertions.assertNotNull(beforeDate);
    }
}