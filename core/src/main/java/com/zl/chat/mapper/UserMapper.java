package com.zl.chat.mapper;

import com.zl.chat.entity.auth.User;
import com.zl.chat.entity.auth.UserRegister;
import org.apache.ibatis.annotations.*;

/**
 * Created by zhangli on 2018/12/26 18:41.</br>
 */
@Mapper
public interface UserMapper {

    String ROW = "id,nick_name nickName,phone_no phoneNo,sex,city,password";

    @Select("<script>"
            + "select " + ROW + " from user where phone_no=#{phone_no}"
            + "</script>")
    User selectUserByPhoneNo(@Param("phone_no") String phoneNo);

    @Select("<script>"
            + "select " + ROW + " from user where id=#{id}"
            + "</script>")
    User selectUserById(@Param("id") String id);

    @Insert("insert into user(id,phone_no,password) values(#{id},#{phoneNo},#{password})")
    void insertUser(UserRegister user);

}
