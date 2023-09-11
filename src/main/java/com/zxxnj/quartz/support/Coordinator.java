package com.zxxnj.quartz.support;

import com.zxxnj.quartz.common.Constants;
import com.zxxnj.quartz.common.utils.DateUtil;
import com.zxxnj.quartz.common.utils.MathUtil;
import com.zxxnj.quartz.common.utils.NetUtil;
import com.zxxnj.quartz.support.assignor.StickyAssignor;
import com.zxxnj.quartz.support.dao.SchInstanceRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static com.zxxnj.quartz.common.utils.SchedulerRepositorySupport.schNames;
import static java.util.stream.Collectors.toList;


/**
 * Coordinator
 *
 * @author zxxnj
 */
@Slf4j
public class Coordinator {

    private SchInstanceRepository schInstanceRepository;

    private InitializeScheduler initializeScheduler;

    private StickyAssignor stickyAssignor;

    private int instanceCount;

    private int schedulerSize;

    public Coordinator(SchInstanceRepository schInstanceRepository, InitializeScheduler initializeScheduler, StickyAssignor stickyAssignor, int instanceCount, int schedulerSize) {
        this.schInstanceRepository = schInstanceRepository;
        this.initializeScheduler = initializeScheduler;
        this.stickyAssignor = stickyAssignor;
        this.instanceCount = instanceCount;
        this.schedulerSize = schedulerSize;
    }

    public void lookup() {
            try {
                String instanceId = NetUtil.generateInstance();
                long currentTime = DateUtil.current();
                //查找未启动的当前实例
                List<Integer> list = schInstanceRepository.lookupIdForNotAssign(instanceId);
                if (!list.isEmpty()) {
                    initializeScheduler.startSchedulerByGroup(list);
                    schInstanceRepository.updateNotAssign(currentTime, instanceId);
                    return;
                }
                currentTime = DateUtil.current();
                //查找当前实例拥有的sch
                List<Integer> assignList = schInstanceRepository.lookupIdByInstanceId(instanceId, currentTime, Constants.THREE * DynamicSchedulerInitializer.TASK_INTERVAL_DELAY);
                //如果当前实例未拥有
                if (assignList.isEmpty()) {
                    int count = schInstanceRepository.countSchInstance(currentTime, Constants.THREE * DynamicSchedulerInitializer.TASK_INTERVAL_DELAY);
                    if (count != schedulerSize) {
                        initializeScheduler.initScheduler();
                    }
                    currentTime = DateUtil.current();
                    //查找type=1的实例
                    List<Integer> typeList = schInstanceRepository.lookupIdByType(Constants.TYPE_ONE);
                    if (typeList.isEmpty()) {
                        return;
                    }
                    //查找存活的实例数
                    List<String> onlineInstanceIds = schInstanceRepository.getAllOnlineInstanceId(currentTime, Constants.THREE * DynamicSchedulerInitializer.TASK_INTERVAL_DELAY);
                    int assignInstances = onlineInstanceIds.stream().distinct().collect(Collectors.toList()).size();
                    if (assignInstances >= instanceCount) {
                        return;
                    }
                    int numAssignorPerInstance = MathUtil.divideToCeil(typeList.size(), instanceCount - assignInstances);
                    List<Integer> instances = stickyAssignor.stickyRebalanced(numAssignorPerInstance);
                    initializeScheduler.startSchedulerByGroup(instances);
                    return;
                }

                if (assignList.size() < schNames.size()) {
                    List<Integer> reduce = schNames.stream().filter(item -> !assignList.contains(item)).collect(toList());
                    initializeScheduler.stopSchedulerByGroup(reduce);
                }
            } catch (Exception e) {
                log.error("Coordinator lookup error.", e);
            }
    }
}
