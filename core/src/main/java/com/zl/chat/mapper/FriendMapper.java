package com.zl.chat.mapper;

import com.zl.chat.entity.auth.User;
import com.zl.chat.entity.auth.UserRegister;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface FriendMapper {

    String ROW = "id,master_id,friend_id,custom_id";

    String USER_ROW = "u.id,u.nick_name nickName,u.phone_no phoneNo,u.sex,u.city";

    @Select("<script>"
            + "select " + USER_ROW + " from user u,friend f where f.master_id=#{id}"
            + "</script>")
    List<User> selectAllFriend(@Param("id") String id);

    @Insert("insert into friend(master_id,friend_id,custom_id) values(#{id},#{phoneNo},#{password})")
    void insertFriendRelationship(UserRegister user);

}
