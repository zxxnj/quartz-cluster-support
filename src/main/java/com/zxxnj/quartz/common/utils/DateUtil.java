package com.zxxnj.quartz.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author zxxnj
 * 时间工具类
 */
public class DateUtil {

    private static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前年月日时分秒的时间
     *
     * @return
     */
    public static String getCurrentTime() {
        return LocalDateTime.now().format(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 当前时间的时间戳毫秒数
     *
     * @return 时间
     */
    public static long current() {
        return System.currentTimeMillis();
    }
}
