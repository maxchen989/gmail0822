package com.max.gmall0822.service;

import com.max.gmall0822.bean.UserAddress;
import com.max.gmall0822.bean.UserInfo;

import java.util.List;

public interface UserService {
    List<UserInfo> getUserInfoListAll();

    void addUser(UserInfo userInfo);

    void updateUser(UserInfo userInfo);

    void updateUserByName(String name, UserInfo userInfo);

    void delUser(UserInfo userInfo);

    public UserInfo getUserInfoById(String Id);

    public UserInfo login(UserInfo userInfo);

    public Boolean verify(String userId);

    public List<UserAddress> getUserAddressList(String userId);
}
