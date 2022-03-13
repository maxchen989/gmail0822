package com.max.gmall0822.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CartInfo implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    String id;
    @Column
    String userId;
    @Column
    String skuId;
    @Column
    BigDecimal cartPrice;
    @Column
    Integer skuNum;
    @Column
    String imgUrl;
    @Column
    String skuName;

    // 实时价格
    @Transient
    BigDecimal skuPrice;
    // 下订单的时候，商品是否勾选 , 放緩存裡 (數據庫結構可能是舊版,新版老師覺得放緩存好)
    @Transient
    String isChecked="0";

}
