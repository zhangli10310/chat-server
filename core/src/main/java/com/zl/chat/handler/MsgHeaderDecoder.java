package com.zl.chat.handler;

import com.zl.chat.msg.MsgHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.InputStream;
import java.util.List;

/**
 * Created by zhangli on 2018/12/20 11:34.</br>
 */
public class MsgHeaderDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        final MsgHeader msgXp = new MsgHeader();
        final InputStream socketInput = new ByteBufInputStream(in);
        boolean ret = msgXp.decode(socketInput);
        IOUtils.closeQuietly(socketInput);

        if (ret) {
            out.add(msgXp);
        }
    }
}
