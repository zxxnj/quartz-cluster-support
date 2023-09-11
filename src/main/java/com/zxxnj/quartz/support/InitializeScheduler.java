package com.zxxnj.quartz.support;

import com.zxxnj.quartz.common.Constants;
import com.zxxnj.quartz.support.assignor.RangeAssignor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static com.zxxnj.quartz.common.utils.SchedulerRepositorySupport.schNames;
import static com.zxxnj.quartz.common.utils.SchedulerRepositorySupport.schedulers;

/**
 * 初始化quartz的Scheduler
 *
 * @author zxxnj
 */
@Component
@Slf4j
public class InitializeScheduler {

    @Resource
    private RangeAssignor assignor;

    @Resource(name = "quartzDataSource")
    private DataSource dataSource;

    @Resource
    private InitializeSchema initializeSchema;

    @Resource
    private AutowireCapableBeanFactory capableBeanFactory;

    public void initScheduler() {
        try {
            List<Integer> lists = assignor.assign();
            if (lists != null && !lists.isEmpty()) {
                startSchedulerByGroup(lists);
            }
        } catch (Exception e) {
            log.error("start Scheduler Exception.", e);
        }
    }

    /**
     * @Description 构建调度任务qurtz框架处理程序SchedulerFactoryBean
     */
    public SchedulerFactoryBean creatScheduler(Integer number) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        try {
            schedulerFactoryBean.setQuartzProperties(quartzProperties(number));
            schedulerFactoryBean.setDataSource(dataSource);
            schedulerFactoryBean.setJobFactory(new AdaptableJobFactory() {
                @Override
                protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
                    final Object jobInstance = super.createJobInstance(bundle);
                    capableBeanFactory.autowireBean(jobInstance);
                    return jobInstance;
                }
            });
            //设置覆盖已存在的任务
            schedulerFactoryBean.setOverwriteExistingJobs(true);
            //项目启动完成后，等待2秒后开始执行调度器初始化
            schedulerFactoryBean.setStartupDelay(2);
            //设置调度器自动运行
            schedulerFactoryBean.setAutoStartup(true);
            schedulerFactoryBean.setOverwriteExistingJobs(true);
        } catch (final IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException("creat Scheduler error");
        }
        return schedulerFactoryBean;
    }

    /**
     * @Description qurtz框架相关配置文件设置，设置其表前缀以及处理程序名称
     */
    public Properties quartzProperties(Integer number) throws IOException {
        final PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        propertiesFactoryBean.getObject().put("org.quartz.jobStore.tablePrefix", "QRTZ" + number + "_");
        propertiesFactoryBean.getObject().put("org.quartz.scheduler.instanceName", "s" + number);
        return propertiesFactoryBean.getObject();
    }


    public void startScheduler(Integer number) throws Exception {
        if (0 != Optional.ofNullable(number).orElse(0)) {
            if (schNames.contains(number)) {
                return;
            }
            schNames.add(number);
        }
        SchedulerFactoryBean schedulerFactoryBean = creatScheduler(number);
        initializeSchema.initSchedulerGroupTable(number);
        schedulerFactoryBean.afterPropertiesSet();
        schedulerFactoryBean.start();
        schedulers.add(schedulerFactoryBean.getScheduler());
    }

    public void startSchedulerByGroup(List<Integer> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            startScheduler(list.get(i));
        }
    }

    public void stopSchedulerByGroup(List<Integer> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            stopScheduler(list.get(i));
        }
    }

    public void stopScheduler(Integer number) throws Exception {
        if (0 != Optional.ofNullable(number).orElse(0)) {
            if (!schNames.contains(number)) {
                return;
            }
            schNames.removeIf(a -> a.equals(number));
        }
        log.error("stopScheduler:{}", number);
        Scheduler scheduler;
        for (int i = 0; i < schedulers.size(); i++) {
            scheduler = schedulers.get(i);
            if (scheduler.getSchedulerName().equals(Constants.QUARTZ_SCHED_NAME_PREFIX + number)) {
                schedulers.remove(scheduler);
                scheduler.shutdown();
                break;
            }
        }
    }

}
