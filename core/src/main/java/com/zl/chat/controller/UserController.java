package com.zl.chat.controller;

import com.zl.chat.entity.auth.User;
import com.zl.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhangli on 2017/7/27 23:33.<br/>
 */
@RestController("/auth")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/login")
//    @Transactional  // 需要事务的时候加上
    public User login(@RequestBody User user) {
        return null;
    }
}
