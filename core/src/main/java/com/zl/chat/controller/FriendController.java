package com.zl.chat.controller;

import com.zl.chat.entity.auth.User;
import com.zl.chat.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by zhangli on 2018/12/30 17:49.</br>
 */
@RestController
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping("/friend/all")
    public Object selectAllFriend(@RequestParam("id") String id){
        return friendService.selectAllFriend(id);
    }
}
