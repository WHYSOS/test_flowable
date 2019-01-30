package com.dt.flowable.bean;

import lombok.Data;

/**
 * @Auther: dengtao
 * @Date: 2019/1/29 14:44
 * @Description:
 */
@Data
public class Purchase {

    private String processInstanceId;

    private String userId;

    private Integer budget;

    //0:紧急采购 1:集中采购 2:个人自购
    private String purchaseType;


}
