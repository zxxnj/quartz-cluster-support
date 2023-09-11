package com.zxxnj.quartz.support.assignor;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * @author zxxnj
 * @Description Quartz处理程序Scheduler分配策略
 */
public abstract class AbstractAssignor {
    //为简化操作，可手动指定实例个数
    @Value("${instanceCount:4}")
    protected int instanceCount;

    @Value("${scheduler.size:20}")
    protected int schedulerSize;

    /**
     * 分配策略
     *
     * @return List<Integer> 分配哪些处理程序
     */
      abstract List<Integer> assign();

}
