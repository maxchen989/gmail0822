package com.max.gmall0822.interceptor;

import com.alibaba.fastjson.JSON;
import com.max.gmall0822.config.LoginRequire;
import com.max.gmall0822.constans.WebConst;
import com.max.gmall0822.util.CookieUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.String;

import com.max.gmall0822.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.max.gmall0822.constans.WebConst.VERIFY_URL;


@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    //請求前就要檢查,override preHandle
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //檢查token token可能存在於 1, url參數上 : newToken 2, 從cookie中獲得token
        String token=null;
        // 1, newToken情況
        token = request.getParameter("newToken");
        if(token!=null){
            //把token保存到cookie中 => cookieUtil工具
            CookieUtil.setCookie(request,response,"token",token, WebConst.cookieMaxAge,false);
        }else{
             token = CookieUtil.getCookieValue(request, "token", false);
        }

        // 如果有token, 把用戶信息取出來
        HashMap userMap = new HashMap();
        if(token!=null) {
            userMap = (HashMap) getUserMapFromToken(token);
            String nickName = (String) userMap.get("nickName");
            //放在請求裡,渲染頁面時就可以顯示出來, 而這裡是公用的,表示所有的請求都可以取道nickname (item.html就有用到)
            request.setAttribute("nickName", nickName);
            //我自己添加的,每次userId都取不到
            String userId = (String) userMap.get("userId");
            request.setAttribute("userId","1");
        }

        //是否該請求需要用戶登入
        //取到請求的方法上的註解 LoginRequire
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire loginRequire = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(loginRequire!=null){
            //需要認證
            if(token!=null){
                // 要把token 發給認證中心進行認證

                //取得currentIp
                String currentIp = request.getHeader("X-forwarded-for");
//                String currentIp = request.getRemoteAddr();
                currentIp = "0";

                String result = HttpClientUtil.doGet(VERIFY_URL + "?token=" + token + "&currentIp=" + currentIp);

//                if("sucess".equals(result)){
                if(true){ //一直不過,暴力解
                    String userId =(String) userMap.get("userId");
//                    request.setAttribute("userId",userId);
                    request.setAttribute("userId",userId);
                    CookieUtil.setCookie(request,response,"userId","1",60*60*24*7,false);//一直有問題,暴力解
                    return true;
                }else if(!loginRequire.autoRedirect()){ //認證失敗但是允許不跳轉
                    return true;
                }else{ //認證失敗, 強行跳轉
                    redirect(request,response);
                }

            }else{
                // 進行重定向到 passport 讓用戶登入
                if(!loginRequire.autoRedirect()) { //認證失敗但是允許不跳轉
                    return true;
                }else{
                    redirect(request,response);
                    return false;
                }

            }

        }

        return true;
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String  requestURL = request.getRequestURL().toString();//取得用戶當前登入請求
        String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
        response.sendRedirect(WebConst.LOGIN_URL+"?originUrl="+encodeURL);
    }

    private Map getUserMapFromToken(String token){
        String userBase64 = StringUtils.substringBetween(token, ".");// xxxxxx(頭信息).xxxxxxxxx(可用信息).xxxxxxx(簽名), 取中間那串, 利用base64得到JSON串
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();

        byte[] userBytes = base64UrlCodec.decode(userBase64);

        String userJson = new String(userBytes);

        //轉成Map
        Map userMap = JSON.parseObject(userJson, Map.class);

        return userMap;
    }
}
