package com.max.gmall0822.bean;

import com.max.gmall0822.enums.OrderStatus;
import com.max.gmall0822.enums.PaymentWay;
import com.max.gmall0822.enums.ProcessStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderInfo implements Serializable {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String consignee;

    @Column
    private String consigneeTel;

    @Column
    private BigDecimal totalAmount;

    @Column
    private OrderStatus orderStatus;

    @Column
    private ProcessStatus processStatus;

    @Column
    private String userId;

    @Column
    private PaymentWay paymentWay;

    @Column
    private Date expireTime;

    @Column
    private String deliveryAddress;

    @Column
    private String orderComment;

    @Column
    private Date createTime;

    @Column
    private String parentOrderId;

    @Column
    private String trackingNo;


    @Transient
    private List<OrderDetail> orderDetailList;


    @Transient
    private String wareId;

    @Column
    private String outTradeNo;

    //訂單總額
    public void sumTotalAmount(){
        BigDecimal totalAmount=new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount= totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
        }
        this.totalAmount=  totalAmount;
    }

}
