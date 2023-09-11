package com.zxxnj.quartz.support;

import com.zxxnj.quartz.common.Constants;
import com.zxxnj.quartz.common.utils.DateUtil;
import com.zxxnj.quartz.common.utils.NetUtil;
import com.zxxnj.quartz.support.dao.SchInstanceRepository;

/**
 * health report
 *
 * @author zxxnj
 */

public class HealthReporter {

    private SchInstanceRepository schInstanceRepository;

    public HealthReporter(SchInstanceRepository schInstanceRepository) {
        this.schInstanceRepository = schInstanceRepository;
    }

    public void healthReporterTask() {
            String instanceId = NetUtil.generateInstance();
            long currentTime = DateUtil.current();
            schInstanceRepository.updateHealthReport(currentTime, instanceId);
            schInstanceRepository.clearHealthReport(currentTime, Constants.THREE * DynamicSchedulerInitializer.TASK_INTERVAL_DELAY);
    }


}
