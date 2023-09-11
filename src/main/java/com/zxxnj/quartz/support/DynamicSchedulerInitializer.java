package com.zxxnj.quartz.support;

import com.zxxnj.quartz.support.assignor.StickyAssignor;
import com.zxxnj.quartz.support.dao.SchInstanceRepository;
import com.zxxnj.quartz.support.dao.SchStateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author zxxnj
 * 动态分配quartz的处理程序
 */
@Component
@Slf4j
public class DynamicSchedulerInitializer implements InitializingBean {

    @Value("${scheduler.size:0}")
    private int schedulerSize;

    @Value("${instanceCount:4}")
    protected int instanceCount;

    @Resource
    private InitializeSchema initializeSchema;

    @Resource
    private SchInstanceRepository schInstanceRepository;

    @Resource
    private SchStateRepository schStateRepository;

    @Resource
    StickyAssignor stickyAssignor;

    @Resource
    private InitializeScheduler initializeScheduler;

    public static long TASK_INTERVAL_DELAY = 10000;

    public static long CLUSTER_INTERVAL_DELAY = 30000;

    public static long COORDINATOR_INTERVAL_DELAY = 60000;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (schedulerSize <= 0) {
            throw new IllegalArgumentException("Please check the configuration of name [scheduler.size]!!!");
        }
        initializeSchema.createTable();
        initializeScheduler.initScheduler();
        HealthReporter healthReporter = new HealthReporter(schInstanceRepository);
        ClusterManager clusterManager = new ClusterManager(schStateRepository, schInstanceRepository, stickyAssignor, schedulerSize);
        Coordinator coordinator = new Coordinator(schInstanceRepository, initializeScheduler, stickyAssignor, instanceCount, schedulerSize);

        ThreadFactory springThreadFactory = new CustomizableThreadFactory("dynamic-manager-");
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1, springThreadFactory);
        threadPoolExecutor.scheduleWithFixedDelay(healthReporter::healthReporterTask, 0, TASK_INTERVAL_DELAY, TimeUnit.MILLISECONDS);
        threadPoolExecutor.scheduleWithFixedDelay(clusterManager::manage, 0, CLUSTER_INTERVAL_DELAY, TimeUnit.MILLISECONDS);
        threadPoolExecutor.scheduleWithFixedDelay(coordinator::lookup, 0, COORDINATOR_INTERVAL_DELAY, TimeUnit.MILLISECONDS);
    }
}
