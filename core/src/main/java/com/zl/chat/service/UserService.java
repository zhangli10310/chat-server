package com.zl.chat.service;

import com.zl.chat.entity.auth.User;
import com.zl.chat.entity.auth.UserRegister;
import com.zl.chat.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by zhangli on 2018/12/26 19:55.</br>
 */
@Service
public class UserService {

    @Autowired
    UserMapper userMapper;

    public User selectUserByPhoneNo(String phoneNo) {
        return userMapper.selectUserByPhoneNo(phoneNo);
    }

    public User selectUserById(String id) {
        return userMapper.selectUserById(id);
    }

    public void insertUser(UserRegister user) {
        userMapper.insertUser(user);
    }
}
