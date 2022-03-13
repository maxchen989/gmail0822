package com.max.gmall0822.user.controller;

import com.max.gmall0822.bean.UserInfo;
import com.max.gmall0822.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("allusers")
    public List<UserInfo> getAllUsers() {
        return userService.getUserInfoListAll();
    }

    @PostMapping("adduser")
    public String addUser(UserInfo userInfo) {
        userService.addUser(userInfo);
        return "sucess";
    }

    @PostMapping("updateUser")
    public String updateUser(UserInfo userInfo) {
        userService.updateUser(userInfo);
        return "sucess";
    }

    @PostMapping("updateUserByName")
    public String updateUserByName(UserInfo userInfo) {
        userService.updateUserByName(userInfo.getName(), userInfo);
        return "sucess";
    }

    @PostMapping("delUser")
    public String delUser(UserInfo userInfo) {
        userService.delUser(userInfo);
        return "sucess";
    }

    @GetMapping("getUser")
    public UserInfo getUser(String id) {
        return userService.getUserInfoById(id);
    }
}
