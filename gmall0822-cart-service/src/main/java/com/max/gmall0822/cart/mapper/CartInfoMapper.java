package com.max.gmall0822.cart.mapper;

import com.max.gmall0822.bean.CartInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    public List<CartInfo> selectCartListWithSkuPrice(String userId);

    public void mergeCartList(@Param("userIdDest") String  userIdDest, @Param("userIdOrig") String userIdOrig);
}
