package com.max.gmall0822.service;

import com.max.gmall0822.bean.*;

import java.util.List;
import java.util.Map;

public interface ManageService {

    //查詢一級分類
    public List<BaseCatalog1> getCatalog1();

    //查詢二級分類 (根據一級分類)
    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    //查詢三級分類 (根據二級分類)
    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    //根據三級分類查詢平台屬性
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    //根據平台屬性id 查詢平台屬性的詳情 順便把該屬性的屬性值列表也取到
    public BaseAttrInfo getBaseAttrInfo(String attrId);

    //保存平台屬性
    public void savaBaseAttrInfo(BaseAttrInfo baseAttrInfo);

    //刪除平台屬性

    //獲得常用銷售屬性
    public List<BaseSaleAttr> getBaseSaleAttrList();

    //保存Spu訊息
    public void safeSpuInfo(SpuInfo spuInfo);

    //根據三級分類查詢spu列表
    public List<SpuInfo> getSpuList(String catalog3Id);

    //根據spuId查詢圖片列表
    public List<SpuImage> getSpuImageList(String spuId);

    //根據spuId查詢銷售屬性
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    //保存skuInfo
    public void saveSkuInfo(SkuInfo skuInfo);

    //查詢skuInfo
    public SkuInfo getSkuInfo(String skuId);

    //根據spuId查詢銷售屬性, 選中傳入的sku涉及的銷售屬性
    public List<SpuSaleAttr> getSpuSaleAttrListBySupIdCheckSku(String skuId, String spuId);

    //根據spuId查詢已有sku涉及的銷售屬性清單
    public Map getSkuValueIdsMap(String spuId);

    //根據多個屬性值查詢平台屬性
    public List<BaseAttrInfo> getAttrList(List attrValueIdList);

}
