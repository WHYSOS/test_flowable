package com.dt.flowable.handler;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * @Auther: dengtao
 * @Date: 2019/1/29 12:03
 * @Description:
 */
public class DivisionChiefHandler implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setAssignee("处长");
    }
}
