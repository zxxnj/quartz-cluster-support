package com.zxxnj.quartz.common.utils;

/**
 *
 * @author zxxnj
 *
 */
public class MathUtil {

    public static int divideToCeil(int x, int y) {
        return (x + y - 1) / y;
    }
}
