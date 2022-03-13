package com.max.gmall0822.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.max.gmall0822.bean.SkuInfo;
import com.max.gmall0822.bean.SpuSaleAttr;
import com.max.gmall0822.config.LoginRequire;
import com.max.gmall0822.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    ManageService manageService;

    @GetMapping("{skuId}.html")
    @LoginRequire(autoRedirect = true)
    public String item(@PathVariable("skuId") String skuId, HttpServletRequest request) {
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListBySupIdCheckSku(skuId, skuInfo.getSpuId());
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("spuSaleAttrList", spuSaleAttrList);
        //得到屬性組合與skuId的映射關係, 用於頁面根據屬性組合進行跳轉
        Map skuValueIdsMap = manageService.getSkuValueIdsMap(skuInfo.getSpuId());
        //轉成JSON好使用
        String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
        request.setAttribute("valuesSkuJson", valuesSkuJson);
        //測試
        request.setAttribute("gname", "<span style=\"color:green\">宝强</span>");
        return "item";
    }
}
