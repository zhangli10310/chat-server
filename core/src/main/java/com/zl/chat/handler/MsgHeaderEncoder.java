package com.zl.chat.handler;

import com.zl.chat.msg.MsgHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by zhangli on 2018/12/20 11:51.</br>
 */
public class MsgHeaderEncoder extends MessageToByteEncoder<MsgHeader> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MsgHeader msg, ByteBuf out) throws Exception {
        out.writeBytes(msg.encode());
    }
}
