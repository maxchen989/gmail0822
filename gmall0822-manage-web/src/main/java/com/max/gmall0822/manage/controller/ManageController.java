package com.max.gmall0822.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.max.gmall0822.bean.*;
import com.max.gmall0822.service.ListService;
import com.max.gmall0822.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@CrossOrigin
public class ManageController {

    @Reference
    ManageService manageService;

    @Reference
    ListService listService;

    @PostMapping("getCatalog1")
    public List<BaseCatalog1> getBaseCatalog1() {
        List<BaseCatalog1> baseCatalog1List = manageService.getCatalog1();
        return baseCatalog1List;
    }

    @PostMapping("getCatalog2")
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id) {
        List<BaseCatalog2> baseCatalog2List = manageService.getCatalog2(catalog1Id);
        return baseCatalog2List;
    }

    @PostMapping("getCatalog3")
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id) {
        List<BaseCatalog3> baseCatalog3List = manageService.getCatalog3(catalog2Id);
        return baseCatalog3List;
    }

    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> getBaseAttrInfoList(String catalog3Id) {
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrList(catalog3Id);
        return baseAttrInfoList;
    }

    @PostMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {

        manageService.savaBaseAttrInfo(baseAttrInfo);
        return "sucess";
    }

    @PostMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId) {

        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        return attrValueList;
    }

    @PostMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = manageService.getBaseSaleAttrList();
        return baseSaleAttrList;

    }

    @PostMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        manageService.safeSpuInfo(spuInfo);
        return "sucess";
    }

    @GetMapping("spuList")
    public List<SpuInfo> getSpuList(String catalog3Id) {
        return manageService.getSpuList(catalog3Id);
    }

    @GetMapping("spuImageList")
    public List<SpuImage> getSupImageList(String spuId) {
        return manageService.getSpuImageList(spuId);
    }

    @GetMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
        return manageService.getSpuSaleAttrList(spuId);
    }

    public String onSaleBySpu(String spuId){

        //HW : 根據spu把其下所有sku上架
        return null;

    }


    @PostMapping("onSale")
    public String onSale(@RequestParam("skuId") String skuId){

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        SkuLsInfo skuLsInfo = new SkuLsInfo();


        try {
            //
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        listService.saveSkuLSInfo(skuLsInfo);

        return "sucess";
    }
}
