package com.dt.flowable.dao;

import com.dt.flowable.bean.Purchase;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


/**
 * @Auther: dengtao
 * @Date: 2019/1/29 15:04
 * @Description:
 */
@Mapper
public interface PurchaseDao {

    @Select("SELECT * FROM purchase WHERE processInstanceId = #{processInstanceId}")
    Purchase getPurchase(Purchase purchase);

    @Insert("INSERT INTO purchase VALUES (#{processInstanceId},#{userId}, #{budget},#{purchaseType})")
    void insertPurchase(Purchase purchase);
}
