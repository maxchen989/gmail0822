package com.max.gmall0822.service;

import com.max.gmall0822.bean.SkuLsInfo;
import com.max.gmall0822.bean.SkuLsParams;
import com.max.gmall0822.bean.SkuLsResult;

public interface ListService {

    public void saveSkuLSInfo(SkuLsInfo skuLsInfo);

    //參數根據商品查詢dsl選擇,因為很多,所以寫成一個Bean
    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams);


}
