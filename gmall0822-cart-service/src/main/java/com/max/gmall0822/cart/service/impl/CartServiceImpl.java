package com.max.gmall0822.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.max.gmall0822.bean.CartInfo;
import com.max.gmall0822.bean.SkuInfo;
import com.max.gmall0822.cart.mapper.CartInfoMapper;
import com.max.gmall0822.service.CartService;
import com.max.gmall0822.service.ManageService;
import com.max.gmall0822.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;

    @Override
    public CartInfo addCart(String userId, String skuId, Integer num) {

        // 加數據
        // 嘗試取出已有的數據, 如果有把數據更新update, 如果沒有insert
        CartInfo cartInfoQuery = new CartInfo();
        cartInfoQuery.setSkuId(skuId);
        cartInfoQuery.setUserId(userId);
        CartInfo cartInfoExists = cartInfoMapper.selectOne(cartInfoQuery);
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        if(cartInfoExists!=null){
            cartInfoExists.setSkuName(skuInfo.getSkuName());
            cartInfoExists.setCartPrice(skuInfo.getPrice());
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum()+num);
            cartInfoExists.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExists);
        }else{
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(num);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExists = cartInfo;
        }

        Jedis jedis = redisUtil.getJedis();
        // 加緩存
        // type : hash, key : cart:101:info field(因為hash是鍵值對,所以使用hash要再考慮field) : skuId value : cartInfoJson
        // 如果購物車中已有該sku就增加個數, 如果沒有則新增一條
        System.out.println("-----------------");
        System.out.println(userId);
        String cartKey = "cart:"+userId+":info";
        String cartInfoJson = JSON.toJSONString(cartInfoExists);
        jedis.hset(cartKey,skuId,cartInfoJson); //新增 ; 也可以當修改會直接覆蓋

        jedis.close();

        return cartInfoExists;

        // 加載數據庫
    }

    @Override
    public List<CartInfo> cartList(String userId) {
        //先查緩存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = "cart:"+userId+":info";
        //找出所屬useridkey=>[skuid,{JSON串}] ;hvals就是得到那個JSON串
        List<String> cartJsonList = jedis.hvals(cartKey);
        List<CartInfo> cartInfoList = new ArrayList<>();
        if(cartJsonList!=null&&cartInfoList.size()>0){//cartInfoList.size()>0嚴謹判斷,他可能是""
            for (String cartJson : cartJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);//將每一個JSON串轉為CartInfo
                cartInfoList.add(cartInfo);
            }

            //越舊得越後面,越新的越前面
            cartInfoList.sort(new Comparator<CartInfo>() {//冒泡排序
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    //前面比後面大時返回1 一樣返回0 後面比前面大時返回-1
                    return o2.getId().compareTo(o1.getId());
                }
            });
            return cartInfoList;

        }else{
            //緩存未命中 //緩存沒有查數據庫,同時加載到緩存中
            return loadCartCache(userId);
        }

    }

    /**
     * 合併購物車
     * @param userIdDest
     * @param userIdOrig
     * @return
     */
    @Override
    public List<CartInfo> mergeCartList(String userIdDest, String userIdOrig) {
        //1 先做合併
        cartInfoMapper.mergeCartList(userIdDest,userIdOrig);
        // 2 合併後把臨時購物車刪除
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userIdOrig);
        cartInfoMapper.delete(cartInfo);
        // 3 重新讀取數據,加載緩存
        List<CartInfo> cartInfoList = loadCartCache(userIdDest);

        return cartInfoList;
    }

    @Override
    public void checkCart(String userId, String skuId, String isChecked) {
        //檢查一下緩存是否存在, 避免因為緩存失效造成緩存和數據庫不一致
        loadCartCacheInfoNoExists(userId);
        // isChecked數據值保存在緩存中
        // 保存標誌
        String cartKey = "cart:" + userId + ":info";
        Jedis jedis = redisUtil.getJedis();

        // Key-> skuId -> json {isChecked}
        String cartInfoJson = jedis.hget(cartKey, skuId);
        //反序列化
        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        //序列化
        String cartInfoJsonNew = JSON.toJSONString(cartInfo);
        jedis.hset(cartKey,skuId,cartInfoJsonNew);

        // 为了订单结账 把所有勾中的商品单独 在存放到一个checked购物车中
        String cartCheckedKey = "cart:" + userId + ":checked";
        if(isChecked.equals("1")){  //勾中加入到待结账购物车中， 取消勾中从待结账购物车中删除
            jedis.hset(cartCheckedKey,skuId,cartInfoJsonNew);
            jedis.expire(cartCheckedKey,60*60);//超過一小時, 勾選就沒了
        }else{
            jedis.hdel(cartCheckedKey,skuId);
        }
        jedis.close();

    }

    @Override
    public List<CartInfo> getCheckedCartList(String userId) {
        // 获得redis中的key
        String cartCheckedKey = "cart:" + userId + ":checked";
        Jedis jedis = redisUtil.getJedis();

        List<String> cartCheckedList = jedis.hvals(cartCheckedKey);
        List<CartInfo> CartInfoList = new ArrayList<>();
        for (String cartJson : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(cartJson,CartInfo.class);
            CartInfoList.add(cartInfo);
        }
        jedis.close();
        return CartInfoList;
    }

    /**
     * 緩存沒有查數據庫,同時加載到緩存中
     * @param userId
     * @return
     */
    public List<CartInfo> loadCartCache(String userId){
        // 讀取數據庫
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithSkuPrice(userId);

//        CartInfo cartInfoQuery = new CartInfo();
//        cartInfoQuery.setUserId(userId);
//        List<CartInfo> cartInfoList = cartInfoMapper.select(cartInfoQuery);
//        //查詢最新價格
//        //如果購物車每個商品都要用迴圈去查詢的話,太消耗數據庫資源(如果購物車裡有100件商品,就要起100條連線,這不是傻?)
//        //所以應該寫sql關聯兩張表,一次查出
//        for (CartInfo cartInfo : cartInfoList) {
//
//        }
        // 加載到緩存中
        Jedis jedis = redisUtil.getJedis();
        //為了方便插入redis 把List 轉換為 Map
        //Map(skuid,JSON)
        if(cartInfoList!=null&&cartInfoList.size()>0) {
            Map<String, String> cartMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                cartMap.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }
            //mset 批量新增
            String cartKey = "cart:" + userId + ":info";
            jedis.del(cartKey); // 清除舊緩存
            jedis.hmset(cartKey, cartMap);    //hash
            jedis.expire(cartKey, 60 * 60 * 24);//過期時間: 一天
            jedis.close();
        }
        return cartInfoList;
    }

    public void loadCartCacheInfoNoExists(String userId){
        String cartKey="cart:"+userId+":Inpfo";
        Jedis jedis = redisUtil.getJedis();
        Long ttl = jedis.ttl(cartKey);
        int ttlInt = ttl.intValue();
        jedis.expire(cartKey,ttlInt+10);
        Boolean exists = jedis.exists(cartKey);
        jedis.close();
        if(!exists){//如果不存在,同步緩存與數據庫
            loadCartCache(userId);
        }

    }
}
