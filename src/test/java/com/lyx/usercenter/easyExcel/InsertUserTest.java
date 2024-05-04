package com.lyx.usercenter.easyExcel;

import cn.hutool.core.util.RandomUtil;
import com.lyx.usercenter.model.domain.User;
import com.lyx.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author timecat
 * @create
 */
@SpringBootTest
class InsertUserTest {

    @Resource
    private UserService userService;

    // 线程设置
    private ExecutorService executorService = new ThreadPoolExecutor(
            8, 1000, 10000, TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(1000));

    /**
     *
     * 批量插入用户   1000  耗时： 907ms
     */
    @Test
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假lyx");
            user.setUserAccount("lyx" + RandomUtil.randomString(3));
            user.setAvatarUrl("https://pic2.zhimg.com/v2-af7a97397d2940f107656415a2bbbf89_r.jpg?source=w");
            user.setProfile("定时任务测试");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123456789108");
            user.setEmail("lyx@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList, 100);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());

    }

    /**
     * 并发批量插入用户   100000  耗时： 7738ms
     */
    @Test
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        // 分十组
        int j = 0;
        //批量插入数据的大小
        int batchSize = 5000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // i 要根据数据量和插入批量来计算需要循环的次数。（鱼皮这里直接取了个值，会有问题,我这里随便写的）
        for (int i = 0; i < INSERT_NUM / batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("假lyx");
                user.setUserAccount("lyx" + RandomUtil.randomString(3));
                user.setAvatarUrl("https://pic2.zhimg.com/v2-af7a97397d2940f107656415a2bbbf89_r.jpg?source=w");
                user.setProfile("定时任务测试");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123456789108");
                user.setEmail("lyx@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());

    }

}