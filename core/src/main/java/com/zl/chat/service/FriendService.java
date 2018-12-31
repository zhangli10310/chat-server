package com.zl.chat.service;

import com.zl.chat.entity.auth.User;
import com.zl.chat.mapper.FriendMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhangli on 2018/12/30 17:48.</br>
 */
@Service
public class FriendService {

    @Autowired
    FriendMapper friendMapper;

    public List<User> selectAllFriend(String id){
        return friendMapper.selectAllFriend(id);
    }
}
