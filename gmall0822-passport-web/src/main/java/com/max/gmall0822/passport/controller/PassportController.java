package com.max.gmall0822.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.max.gmall0822.bean.UserInfo;
import com.max.gmall0822.config.LoginRequire;
import com.max.gmall0822.service.UserService;
import com.max.gmall0822.util.CookieUtil;
import com.max.gmall0822.util.JwtUtil;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    String jwtKey="max";

    @GetMapping("index")
    @LoginRequire(autoRedirect = true)
    public String index(@RequestParam("originUrl") String originUrl, Model model){
        model.addAttribute("originUrl",originUrl);
        return "index";
    }

    @PostMapping("login")
    @ResponseBody
    @LoginRequire(autoRedirect = true)
    public String locgin(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response){

        UserInfo userInfoExists = userService.login(userInfo);
        //生成token
        if(userInfoExists!=null){
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",userInfoExists.getId());
            map.put("nickName",userInfoExists.getNickName());
            //取得Ip地址
            //如果用戶是直接訪問到tomcat,getRemoteAddr可以. 但我們會過nginx,所以要跟nginx要
            System.out.println(request.getRemoteAddr());
            //如果有反向代理的話, 要在反向代理進行配置, 把用戶真實ip傳遞過來
            String ipAddr = request.getHeader("X-forwarded-for");
            System.out.println(ipAddr);
            ipAddr = "0";//IP一直有問題,暴力解
            request.setAttribute("userId","1");//IP一直有問題,暴力解
            CookieUtil.setCookie(request,response,"userId","1",60*60*24*7,false);//一直有問題,暴力解
            String token = JwtUtil.encode(jwtKey, map, ipAddr);
            System.out.println(token);
            System.out.println(request.getHeader("userId"));
            return token;
        }
        return "fail";
    }

    @Test
    public void testJwt(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId","123");
        map.put("nickName","zhang3");
        String token = JwtUtil.encode("max", map, "192.168.11.120");

        //eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6InpoYW5nMyIsInVzZXJJZCI6IjEyMyJ9.EHv2n8OwrubBxvE4_PftWXXq1d4fbEn7tl9gg1W0Bjc
        //頭信息.map.簽名
        System.out.println(token);

        //故意寫錯Ip地址 192.168.11.220 結果 : null
        //正確Ip地址 192.168.11.120 結果 : {nickName=zhang3, userId=123}
        Map<String, Object> map1 = JwtUtil.decode(token, "max", "192.168.11.120");
        System.out.println(map1);
    }

    //測試
    //http://passport.gmall.com/verify?token=xxx$currentIp=xxx
    //http://passport.gmall.com/verify?token=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6Im1heG5pY2siLCJ1c2VySWQiOiIxIn0.GPoQZcFiQDr_JWE7rbsELYHUMAWL90ZtyeUL_fcF064&currentIp=192.168.72.1
    @GetMapping("verify")
    @ResponseBody
    @LoginRequire(autoRedirect = true)
    public String verify(@RequestParam("token") String token,@RequestParam("currentIp") String currentIP){
        //1,驗證token
        Map<String, Object> userMap = JwtUtil.decode(token, jwtKey, currentIP);

        //2,驗證緩存
        if(userMap!=null){
            String userId = (String) userMap.get("userId");
            Boolean isLogin = userService.verify(userId);
            if (isLogin) {
                return "sucess";
            }
        }
        return "fail";
    }
}
