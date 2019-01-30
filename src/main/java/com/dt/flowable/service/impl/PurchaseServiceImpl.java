package com.dt.flowable.service.impl;


import com.dt.flowable.bean.Purchase;
import com.dt.flowable.dao.PurchaseDao;
import com.dt.flowable.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Auther: dengtao
 * @Date: 2019/1/29 15:14
 * @Description:
 */
@Service
public class PurchaseServiceImpl implements PurchaseService {
    @Autowired
    private PurchaseDao purchaseDao;

    public void insertPurchaseS(Purchase purchase){
        purchaseDao.insertPurchase(purchase);
    }

    public Purchase getPurchaseS(Purchase purchase){
        return purchaseDao.getPurchase(purchase);
    }

}
