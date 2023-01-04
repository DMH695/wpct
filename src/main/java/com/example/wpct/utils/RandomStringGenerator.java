package com.example.wpct.utils;

import java.util.Random;


/**
 * @author 陳樂
 * @version 1.0.0
 * @ClassName RandomStringGenerator.java
 * @Description 随机数生成
 * @createTime 2022年04月09日 13:03:00
 */
public class RandomStringGenerator {

    /**
     * 获取一定长度的随机字符串
     *
     * @param length 指定字符串长度
     * @return 一定长度的字符串
     */
    public static String getRandomStringByLength(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

}