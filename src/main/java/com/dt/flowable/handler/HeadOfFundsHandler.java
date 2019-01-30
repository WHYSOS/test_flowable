package com.dt.flowable.handler;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: dengtao
 * @Date: 2019/1/29 10:09
 * @Description:
 */
public class HeadOfFundsHandler implements TaskListener{
    @Override
    public void notify(DelegateTask delegateTask) {
        //delegateTask.setAssignee("经费负责人");
        List<String> userIdList = new ArrayList<>();
        userIdList.add("经费负责人1");
        userIdList.add("经费负责人2");
        delegateTask.addCandidateGroups(userIdList);
    }

}
