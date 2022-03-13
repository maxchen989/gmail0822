package com.max.gmall0822.config;

import com.max.gmall0822.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class webMvcConfig extends WebMvcConfigurerAdapter {

    //註冊一個攔截器
    @Autowired
    AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // /** : 所有路徑進入tomcat都攔截
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
    }
}
