package com.dt.flowable.controller;

import com.dt.flowable.bean.Purchase;
import com.dt.flowable.service.PurchaseService;
import com.dt.flowable.util.FlowableUtil;
import com.google.gson.Gson;
import org.apache.ibatis.annotations.Result;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.spring.integration.Flowable;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 *
 * @auther: dengtao
 * @date: 2019/1/29 11:24
 * @description:
 * 
 */
@Controller
@RequestMapping(value = "flowable")
public class PurchaseController {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private PurchaseService purchaseService;

    @GetMapping("hello")
    @ResponseBody
    public String hello(){
        return "hello";
    }

/***************此处为业务代码******************/
    /**
     * 添加报销
     */
    @PostMapping(value = "add")
    @ResponseBody
    public String addExpense(Purchase purchase) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", purchase.getUserId());


        //启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess", map);

        List<Task> tasks = taskService.createTaskQuery().taskAssignee(purchase.getUserId()).orderByTaskCreateTime().desc().list();
        Task task = null;
        for (Task task1:tasks){
            if (task1.getProcessInstanceId().equals(processInstance.getId())){
                task = task1;
            }
        }

        purchase.setProcessInstanceId(processInstance.getId());
        purchaseService.insertPurchaseS(purchase);

        if (task!=null){
            map.put("auditing","通过");
            taskService.complete(task.getId(), map);
            //runtimeService.setVariable(task.getExecutionId(),"lastUserTaskId",task.getTaskDefinitionKey());
        }
        return "提交成功.流程实例Id为：" + processInstance.getId();
    }


    /**
     * 获取审批管理列表
     */
    @GetMapping(value = "/list")
    @ResponseBody
    public String list(String userId) {
        List<Task> tasksPerson = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
        List<Task> tasksGroup = taskService.createTaskQuery().taskCandidateGroup(userId).list();
        List<HashMap> listPerson = new ArrayList<>();
        List<HashMap> listGroup = new ArrayList<>();
        for (Task task : tasksPerson) {
            HashMap hashMap = new HashMap();
            hashMap.put("用户ID",task.getAssignee());
            hashMap.put("当前流程节点名称",task.getName());
            hashMap.put("任务ID",task.getId());
            hashMap.put("流程实例ID",task.getProcessInstanceId());
            listPerson.add(hashMap);
        }
        for (Task task : tasksGroup) {
            HashMap hashMap = new HashMap();
            hashMap.put("用户ID",task.getAssignee());
            hashMap.put("当前流程节点名称",task.getName());
            hashMap.put("任务ID",task.getId());
            hashMap.put("流程实例ID",task.getProcessInstanceId());
            listGroup.add(hashMap);
        }
        Map<String,Object> response = new HashMap<>();
        response.put("个人任务",listPerson);
        response.put("组内任务",listGroup);
        Gson gson = new Gson();
        String tasksJson = gson.toJson(response);
        return tasksJson;
    }


    /**
     * 批准
     *
     * @param taskId 任务ID
     */
    @PostMapping(value = "apply")
    @ResponseBody
    public String apply(String taskId,String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("流程不存在");
        }
        //如果没有指派任务人，则为组任务，需要认领任务
        if (task.getAssignee() == null){
            //认领任务
            processEngine.getTaskService().claim(taskId,userId);
        }
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        Purchase purchase = new Purchase();
        Integer budget = 50000;
        purchase.setProcessInstanceId(task.getProcessInstanceId());
        purchase = purchaseService.getPurchaseS(purchase);
        if(purchase.getBudget()>budget&&"2".equals(purchase.getPurchaseType())){
            //到处长环节
            runtimeService.setVariable(task.getExecutionId(),"auditingJudge","处长审核");
        }
        else{
            //科长直接通过结束
            runtimeService.setVariable(task.getExecutionId(),"auditingJudge","科长审核结束");
        }

        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        map.put("auditing", "通过");
        taskService.complete(taskId, map);
        //runtimeService.setVariable(task.getExecutionId(),"lastUserTaskId",task.getTaskDefinitionKey());
        return "processed ok!";
    }

    /**
     * 拒绝
     */
    @ResponseBody
    @PostMapping(value = "reject")
    public String reject(String taskId) {
        /*HashMap<String, Object> map = new HashMap<>();
        map.put("auditing", "驳回");
        taskService.complete(taskId, map);*/
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("流程不存在");
        }
       //String lastUserTaskId = runtimeService.getVariable(task.getExecutionId(),"lastUserTaskId").toString();
        //String lastUserTaskId = this.getLastNode(task);
        String lastUserTaskId = FlowableUtil.getLastNode(task);
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveActivityIdTo(task.getTaskDefinitionKey(),lastUserTaskId)
                .changeState();

        return "reject OK";
    }

    /**
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @GetMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

        //流程走完的不显示图
        if (pi == null) {
            return;
        }
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

}
