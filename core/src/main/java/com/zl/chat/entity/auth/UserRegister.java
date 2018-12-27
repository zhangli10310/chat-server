package com.zl.chat.entity.auth;

/**
 * Created by zhangli on 2018/12/27 17:59.</br>
 */
public class UserRegister {

    private String id;
    private String phoneNo;
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
