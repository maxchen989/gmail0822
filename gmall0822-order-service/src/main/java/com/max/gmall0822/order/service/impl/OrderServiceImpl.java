package com.max.gmall0822.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.max.gmall0822.bean.OrderDetail;
import com.max.gmall0822.bean.OrderInfo;
import com.max.gmall0822.order.mapper.OrderDetailMapper;
import com.max.gmall0822.order.mapper.OrderInfoMapper;
import com.max.gmall0822.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Override
    @Transactional
    public void saveOrder(OrderInfo orderInfo) {

        orderInfoMapper.insertSelective(orderInfo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);

        }

    }
}
