package com.zxxnj.quartz.common.utils;

import org.quartz.Scheduler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局获取scheduler
 * @author zxxnj
 */
public class SchedulerRepositorySupport {
    /**
     * 全局处理程序Scheduler获取
     */
    public static List<Scheduler> schedulers = new CopyOnWriteArrayList<>();

    /**
     * 记录名字
     */
    public static List<Integer> schNames = new CopyOnWriteArrayList<>();

    /**
     * 定义获取下标
     */
    public static AtomicInteger index = new AtomicInteger(0);

    /**
     * 获取下标
     * @return
     */
    public static int getIndex() {
        if (schedulers == null || schedulers.size() == 0) {
            return 0;
        }
        if (index.get() == schedulers.size()) {
            index.set(0);
        }
        return index.getAndIncrement() % schedulers.size();
    }

}
