package com.zl.chat.mapper;

import com.zl.chat.entity.auth.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by zhangli on 2018/12/26 18:41.</br>
 */
@Mapper
public interface UserMapper {

    @Select("select * from mcuser")
    List<User> searchUsers();
}
