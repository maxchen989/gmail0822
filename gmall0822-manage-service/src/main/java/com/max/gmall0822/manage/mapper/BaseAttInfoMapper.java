package com.max.gmall0822.manage.mapper;

import com.max.gmall0822.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttInfoMapper extends Mapper<BaseAttrInfo> {
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    List<BaseAttrInfo> getBaseAttrInfoListByValueIds(@Param("valueIds") String valueIds);

}
