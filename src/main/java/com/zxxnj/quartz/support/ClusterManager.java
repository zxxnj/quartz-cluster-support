package com.zxxnj.quartz.support;

import com.zxxnj.quartz.common.Constants;
import com.zxxnj.quartz.common.utils.DateUtil;
import com.zxxnj.quartz.common.utils.NetUtil;
import com.zxxnj.quartz.support.assignor.StickyAssignor;
import com.zxxnj.quartz.support.dao.SchInstanceRepository;
import com.zxxnj.quartz.support.dao.SchStateRepository;
import lombok.extern.slf4j.Slf4j;



/**
 * cluster manager
 *
 * @author zxxnj
 */
@Slf4j
public class ClusterManager {

    private SchStateRepository schStateRepository;

    private SchInstanceRepository schInstanceRepository;

    private StickyAssignor stickyAssignor;

    private int schedulerSize;

    public ClusterManager(SchStateRepository schStateRepository, SchInstanceRepository schInstanceRepository, StickyAssignor stickyAssignor, int schedulerSize) {
        this.schStateRepository = schStateRepository;
        this.schInstanceRepository = schInstanceRepository;
        this.stickyAssignor = stickyAssignor;
        this.schedulerSize = schedulerSize;
    }

    protected boolean firstCheckIn = true;

    protected long signTime = 0L;

    public void manage() {
            String instanceId = NetUtil.generateInstance();
            long currentTime = DateUtil.current();
            if (0 == schStateRepository.updateSchState(instanceId, currentTime)) {
                schStateRepository.insertSchState(instanceId, currentTime);
            }
            lookup();
            schStateRepository.deleteSchState(currentTime, Constants.THREE * DynamicSchedulerInitializer.CLUSTER_INTERVAL_DELAY);
    }

    public void lookup() {
        long currentTime = DateUtil.current();
        int count = schInstanceRepository.countSchInstance(currentTime, Constants.THREE * DynamicSchedulerInitializer.TASK_INTERVAL_DELAY);
        if (count != schedulerSize) {
            if (firstCheckIn) {
                signTime = currentTime;
                firstCheckIn = false;
            }
        } else {
            firstCheckIn = true;
            signTime = 0L;
        }
        if (!firstCheckIn && (currentTime > signTime + Constants.SIX * DynamicSchedulerInitializer.CLUSTER_INTERVAL_DELAY)) {
            stickyAssignor.assign();
            firstCheckIn = true;
            signTime = 0L;
        }
    }
}
