package com.dt.flowable.util;


import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

/**
 * @Auther: dengtao
 * @Date: 2019/1/30 11:23
 * @Description:
 */
@Component
public class FlowableUtil {

    private static RepositoryService repositoryService;

    @Autowired
    private RepositoryService repositoryService1;

    @PostConstruct
    public void beforeInit() {
        repositoryService = repositoryService1;
    }

    /**
     *
     * @auther: dengtao
     * @date: 2019/1/30 15:10
     * @description: 获取上一个userTask的ID
     *
     */
    public static String getLastNode(Task task) {

        String processDefinitionId = task.getProcessDefinitionId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        Process process = bpmnModel.getProcesses().get(0);
        //只获取UserTask节点
        Collection<UserTask> flowElements = process.findFlowElementsOfType(UserTask.class);
        int i = 0;
        for (UserTask userTask : flowElements) {
            //如果遍历userTask节点的id等于当前节点
            if(userTask.getId().equals(task.getTaskDefinitionKey())){
                //返回上一个节点
                return ((List<UserTask>) flowElements).get(i-1).getId();
            }
            i++;
        }
        return null;
    }

}
