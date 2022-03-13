package com.max.gmall0822.user.mapper;

import com.max.gmall0822.bean.UserInfo;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface UserMapper extends Mapper<UserInfo> {
}
