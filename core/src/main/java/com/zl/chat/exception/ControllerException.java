package com.zl.chat.exception;

/**
 * Created by zhangli on 2017/7/28 15:27.</br>
 */
public class ControllerException extends RuntimeException {

    private int code = -1;

    public ControllerException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ControllerException(String message) {
        super(message);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
