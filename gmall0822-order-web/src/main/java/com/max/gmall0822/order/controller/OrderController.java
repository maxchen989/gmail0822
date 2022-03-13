package com.max.gmall0822.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.max.gmall0822.bean.*;
import com.max.gmall0822.config.LoginRequire;
import com.max.gmall0822.enums.OrderStatus;
import com.max.gmall0822.enums.ProcessStatus;
import com.max.gmall0822.service.CartService;
import com.max.gmall0822.service.ManageService;
import com.max.gmall0822.service.OrderService;
import com.max.gmall0822.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.DateUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    ManageService manageService;

    @GetMapping("trade")
    @LoginRequire(autoRedirect = true)
    public String trade(HttpServletRequest request) {
        //獲得用戶信息
        String userId = (String) request.getAttribute("userId");
        //用戶地址列表
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);

        request.setAttribute("userAddressList",userAddressList);

        //用戶需要結帳的商品清單
        List<CartInfo> checkedCartList = cartService.getCheckedCartList(userId);

        //總金額
        BigDecimal totalAmount = new BigDecimal("0");
        for (CartInfo cartInfo : checkedCartList) {
            BigDecimal cartInfoAmount = cartInfo.getSkuPrice().multiply(new BigDecimal((cartInfo.getSkuNum())));
            totalAmount = totalAmount.add(cartInfoAmount);//bigdecimal的加法
        }

        request.setAttribute("checkedCartList",checkedCartList);

        request.setAttribute("totalAmount",totalAmount);

        UserInfo userInfo = userService.getUserInfoById(userId);

        return "trade";
    }

    @PostMapping("submitOrder")
    @LoginRequire(autoRedirect = true)
    public String submitOrder (OrderInfo orderInfo,HttpServletRequest request){
        //剛保存完的訂單狀態 : 未支付
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setCreateTime(new Date());
        //過期時間
        orderInfo.setExpireTime(DateUtils.addMilliseconds(new Date(),15));
        //訂單總額
        orderInfo.sumTotalAmount();
        //userId
        orderInfo.setUserId((String) request.getAttribute("userId"));

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());
            orderDetail.setSkuName(skuInfo.getSkuName());
            //驗價,避免因下單時價格改變, 產生的糾紛 先不管
        }

        orderService.saveOrder(orderInfo);
        return "redirect://payment.gmall.com/index";

    }
}
