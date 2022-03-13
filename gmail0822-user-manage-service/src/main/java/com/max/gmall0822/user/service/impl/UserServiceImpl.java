package com.max.gmall0822.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.max.gmall0822.bean.UserAddress;
import com.max.gmall0822.bean.UserInfo;
import com.max.gmall0822.user.mapper.UserAddressMapper;
import com.max.gmall0822.user.mapper.UserMapper;
import com.max.gmall0822.service.UserService;

import com.max.gmall0822.util.RedisUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UserInfo> getUserInfoListAll() {
        List<UserInfo> userInfoList = userMapper.selectAll();

        return userInfoList;
    }

    @Override
    public void addUser(UserInfo userInfo) {
        userMapper.insertSelective(userInfo);

    }

    @Override
    public void updateUser(UserInfo userInfo) {
        userMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void updateUserByName(String name, UserInfo userInfo) {

        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name", name);
        userMapper.updateByExampleSelective(userInfo, example);
    }

    @Override
    public void delUser(UserInfo userInfo) {
        userMapper.deleteByPrimaryKey(userInfo.getId());
    }

    @Override
    public UserInfo getUserInfoById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Override
    public UserInfo login(UserInfo userInfo) {
        //1, 比對數據庫信息 用戶名和密碼 (數據庫存的是加密後的,因此將用戶傳來的加密後再去查詢數據庫)

        String passwd = userInfo.getPasswd();

        String passwdMd5 = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(passwdMd5);

        UserInfo userInfoExists = userMapper.selectOne(userInfo);

        if(userInfoExists!=null){
            // 2, 加載緩存
            Jedis jedis = redisUtil.getJedis();

            // type String key user:1011:info value userInfoJson
            String userKey = userKey_prefix + userInfoExists.getId() + userinfoKey_suffix;
            String userInfoJSON = JSON.toJSONString(userInfoExists);
            jedis.setex(userKey,userKey_timeOut,userInfoJSON);
            jedis.close();
            return userInfoExists;
        }
            return null;

    }

    @Override
    public Boolean verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userKey = userKey_prefix + userId + userinfoKey_suffix;
        Boolean isLogin = jedis.exists(userKey);
        //避免有人長時間登入,結果被踢掉,因此確認登入,延長過期時間
        if(isLogin){
            jedis.expire(userKey,userKey_timeOut);
        }

        jedis.close();

        return isLogin;
    }

    @Test
    //寫入用戶信息進行測試(我自己加的)
    public void testPassword() {

        UserInfo userInfo = new UserInfo();

        userInfo.setName("max1");
        String passwdMd5 = DigestUtils.md5DigestAsHex("123456788".getBytes());
        System.out.println(passwdMd5);

    }

    public List<UserAddress> getUserAddressList(String userId){

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);
        return userAddressList;
    }

}
