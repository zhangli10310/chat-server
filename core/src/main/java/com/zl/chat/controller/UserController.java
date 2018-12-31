package com.zl.chat.controller;

import com.zl.chat.entity.auth.User;
import com.zl.chat.entity.auth.UserLogin;
import com.zl.chat.entity.auth.UserRegister;
import com.zl.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by zhangli on 2017/7/27 23:33.<br/>
 */
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/auth/login")
//    @Transactional  // 需要事务的时候加上
    public Object login(@RequestBody UserLogin param) {
        User user = null;

        if (param.getAccount() != null) {
            user = userService.selectUserByPhoneNo(param.getAccount());
            if (user == null) {
                user = userService.selectUserById(param.getAccount());
            }
        }

        if (user == null) {
            throw new RuntimeException("用户名错误");
        } else {
            if (String.valueOf(user.getPassword()).equals(param.getPassword())) {
                return user;
            } else {
                throw new RuntimeException("密码错误");
            }
        }
    }

    @PostMapping("/auth/register")
    public Object register(@RequestBody UserRegister param) {
        if (param.getPassword() == null || param.getPassword().isEmpty()) {
            throw new RuntimeException("请设置密码");
        }
        while (true) {
            String s = UUID.randomUUID().toString().replace("-", "");
            int start = s.length() - 20;
            if (start > 0) {
                s = s.substring(start);
            }
            User u = userService.selectUserById(s);
            if (u == null) {
                param.setId(s);
                break;
            }
        }
        userService.insertUser(param);
        return param.getId();
    }

    @GetMapping("/user/queryById")
    public Object queryById(@RequestParam("id") String id) {

        User user = userService.selectUserById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @GetMapping("/user/search")
    public Object search(@RequestParam("content") String content) {
        Set<User> set = new HashSet<>();
        if (content == null || content.isEmpty()) {
            return set;
        }
        User user = userService.selectUserByPhoneNo(content);
        if (user != null) {
            user.setPassword(null);
            set.add(user);
        }
        user = userService.selectUserById(content);
        if (user != null) {
            user.setPassword(null);
            set.add(user);
        }
        return set;
    }
}
