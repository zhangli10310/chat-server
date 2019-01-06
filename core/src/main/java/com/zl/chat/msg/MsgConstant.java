package com.zl.chat.msg;

/**
 * Created by zhangli on 2018/12/24 11:23.</br>
 */
public class MsgConstant {

    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";

    public static final int CMDID_NOOPING = 0x06;
    public static final int CMDID_NOOPING_RESP = 0x06;

    public static final int CMDID_LINK_ACCOUNT_CHANNEL = 0x10; //绑定用户名和channel

    public static final int CMDID_SEND_SINGLE_TEXT_MSG = 0x20; //发送单聊文字消息
    public static final int CMDID_RECEIVE_SINGLE_TEXT_MSG = 0x21; //接收单聊文字消息
}
