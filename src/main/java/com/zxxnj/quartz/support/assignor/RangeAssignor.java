package com.zxxnj.quartz.support.assignor;

import com.zxxnj.quartz.support.assignor.AbstractAssignor;
import com.zxxnj.quartz.support.assignor.AssignorStoreSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zxxnj
 */
@Component
@Slf4j
public class RangeAssignor extends AbstractAssignor {


    @Resource
    private AssignorStoreSupport assignorStoreSupport;

    @Override
    public List<Integer> assign() {
        return assignorStoreSupport.rangeAssign(schedulerSize, instanceCount);
    }
}
