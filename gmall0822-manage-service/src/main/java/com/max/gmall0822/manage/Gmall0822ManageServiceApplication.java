package com.max.gmall0822.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.max.gmall0822.manage.mapper")
@EnableTransactionManagement
@ComponentScan(basePackages = "com.max.gmall0822")
public class Gmall0822ManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Gmall0822ManageServiceApplication.class, args);
    }

}
