package com.lyx.usercenter.easyExcel;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * 导入Excel，读取数据
 *
 * @author timecat
 */
public class ImportExcel {

    public static void main(String[] args) {
        String fileName = "E:\\code2\\find-friends\\find-friend-backend\\src\\main\\java\\com\\lyx\\usercenter\\easyExcel\\testExcel.xlsx";
        synchronousRead(fileName);
    }

    /**
     * 监听器读取
     *
     * @param fileName
     */
    public static void readByListener(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行
        EasyExcel.read(fileName, UserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<UserInfo> list = EasyExcel.read(fileName).head(UserInfo.class).sheet().doReadSync();
        for (UserInfo xingQiuTableUserInfo : list) {
            System.out.println(xingQiuTableUserInfo);
        }

    }
}
