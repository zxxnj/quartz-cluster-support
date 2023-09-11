package com.zxxnj.quartz.support.assignor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zxxnj
 */
@Component
@Slf4j
public class StickyAssignor extends AbstractAssignor {

    @Resource
    private AssignorStoreSupport assignorStoreSupport;

    @Override
    public List<Integer> assign() {
        return assignorStoreSupport.stickyAssign();
    }

    /**
     * 粘性分配平衡
     * @param numAssignorPerInstance
     * @return
     */
    public List<Integer> stickyRebalanced(Integer numAssignorPerInstance) {
        return assignorStoreSupport.stickyRebalanced(numAssignorPerInstance);
    }
}
