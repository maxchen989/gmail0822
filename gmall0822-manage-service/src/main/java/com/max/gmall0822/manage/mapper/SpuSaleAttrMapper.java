package com.max.gmall0822.manage.mapper;

import com.max.gmall0822.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    public List<SpuSaleAttr> getSpuSaleAttrListBySupId(String spuId);

    public List<SpuSaleAttr> getSpuSaleAttrListBySupIdCheckSku(@Param("sku_id") String skuId, @Param("spu_id") String spuId);
}
