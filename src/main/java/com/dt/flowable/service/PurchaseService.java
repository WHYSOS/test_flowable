package com.dt.flowable.service;

import com.dt.flowable.bean.Purchase;

/**
 * @Auther: dengtao
 * @Date: 2019/1/29 15:14
 * @Description:
 */
public interface PurchaseService {

    void insertPurchaseS(Purchase purchase);

    Purchase getPurchaseS(Purchase purchase);
}
