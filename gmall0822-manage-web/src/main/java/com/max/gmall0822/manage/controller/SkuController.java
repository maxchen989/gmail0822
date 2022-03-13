package com.max.gmall0822.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.max.gmall0822.bean.SkuInfo;
import com.max.gmall0822.service.ManageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SkuController {

    @Reference
    ManageService manageService;

    @PostMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        manageService.saveSkuInfo(skuInfo);
        return "sucess";
    }

}
