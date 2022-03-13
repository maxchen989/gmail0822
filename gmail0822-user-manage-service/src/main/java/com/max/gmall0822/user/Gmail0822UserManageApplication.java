package com.max.gmall0822.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.max.gmall0822.user.mapper")
@ComponentScan(basePackages = "com.max.gmall0822")
public class Gmail0822UserManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(Gmail0822UserManageApplication.class, args);
    }

}
