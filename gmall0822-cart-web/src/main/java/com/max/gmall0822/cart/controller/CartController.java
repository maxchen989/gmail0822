package com.max.gmall0822.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.max.gmall0822.bean.CartInfo;
import com.max.gmall0822.config.LoginRequire;
import com.max.gmall0822.service.CartService;
import com.max.gmall0822.util.CookieUtil;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference
    CartService cartService;

    @PostMapping("addToCart")
    @LoginRequire(autoRedirect=true)//購物車不要求依定要登入,所以加參數,不強制跳轉.
    public String addToCart(@RequestParam("skuId") String skuId, @RequestParam("num") int num, HttpServletRequest request, HttpServletResponse response){
        //這裡沒有userId,從哪裡取的? 從攔截器裡面取得! 記得加註解LoginRequire,但因為不強制跳轉,所以可能取不到userId

        String userId = (String) request.getAttribute("userId");
//        if(userId!=null){//用戶有登入
//            cartService.addCart(userId,skuId,num);
//
//        }else {//用戶未登入
//            //如果用戶未登入,檢查用戶是否有token, 如果有token,用token作為id加購物車,如果沒有生成一個新的token放入cookie
//            String user_tmp_id = CookieUtil.getCookieValue(request, "user_tmp_id", false);
//            if(user_tmp_id!=null){
//                cartService.addCart(user_tmp_id,skuId,num);
//            }else {
//                //沒有token 隨機生成
//                user_tmp_id = UUID.randomUUID().toString();
//                cartService.addCart(user_tmp_id,skuId,num);
//            }
//
//        }
        //上面的優化寫法
        if(userId==null){
            //如果用戶未登入,檢查用戶是否有token, 如果有token,用token作為id加購物車,如果沒有生成一個新的token放入cookie
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);

            if(userId==null){
                //沒有token 隨機生成
                userId = UUID.randomUUID().toString();
                //寫入Cookie
                CookieUtil.setCookie(request,response,"user_tmp_id",userId,60*60*24*7,false);
            }

        }
        CartInfo cartInfo = cartService.addCart(userId, skuId, num);
        request.setAttribute("cartInfo",cartInfo);
        //當前的數量
        request.setAttribute("num",num);

        return "success";


    }

    @GetMapping("cartList")
    @LoginRequire(autoRedirect = true)
    public String cartList(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId"); //查看用戶登入id

        if (userId != null) {//有登入
            List<CartInfo> cartList = null; //如果登入前(未登入)時,存在臨時購物車,要考慮合併
            String userTemId = CookieUtil.getCookieValue(request, "user_tmp_id", false); //取臨時id
            if (userTemId != null) {
                List<CartInfo> cartTempInfoList = cartService.cartList(userTemId); //如果有臨時id, 查是否有臨時購物車
                if (cartTempInfoList != null && cartTempInfoList.size() > 0) {
                    cartList = cartService.mergeCartList(userId, userTemId);//如果有臨時購物車, 那麼進行合併, 並且獲得合併後的購物車列表
                }
            }
            if (cartList == null || cartList.size() == 0) {
                cartList = cartService.cartList(userId);//如果不需要合併, 再取登入後的購物車
            }
            request.setAttribute("cartList", cartList);
        } else { //未登入, 直接取臨時購物車
            String userTemId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
            if (userTemId != null) {
                List<CartInfo> cartTempInfoList = cartService.cartList(userTemId);
                request.setAttribute("cartList", cartTempInfoList);
            }
        }
        return "cartList";
    }

    @PostMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    //返回值要返回什麼?看javascript, 這邊沒用上
    public void checkCart(@RequestParam("isChecked") String isChecked,@RequestParam("skuId") String skuId,HttpServletRequest request ){

        String userId = (String) request.getAttribute("userId");
        userId = CookieUtil.getCookieValue(request, "userId", false);//一直有問題,暴力解
        if(userId==null){
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false); //取臨時id
        }

        cartService.checkCart(userId,skuId,isChecked);

    }
}
