package com.max.gmall0822.service;

import com.max.gmall0822.bean.CartInfo;

import java.util.List;

public interface CartService {

    public CartInfo addCart(String userId, String skuId, Integer num);

    public List<CartInfo> cartList(String userId);

    public List<CartInfo> mergeCartList(String userIdDest, String userIdOrig);

    public void checkCart(String userId,String skuId,String isChecked);

    public List<CartInfo> getCheckedCartList(String userId);
}
