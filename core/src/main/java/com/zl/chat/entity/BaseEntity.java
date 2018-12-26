package com.zl.chat.entity;

/**
 * Created by zhangli on 2017/7/28 15:12.</br>
 */
public class BaseEntity<T> {

    private int code;  //成功就是0,其他都是不成功
    private T data;
    private String message;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
