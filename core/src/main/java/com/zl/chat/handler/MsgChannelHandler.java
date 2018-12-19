package com.zl.chat.handler;

import com.zl.chat.GlobalUserUtil;
import com.zl.chat.msg.NetMsgHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class MsgChannelHandler extends ChannelInboundHandlerAdapter {


    private static final Logger LOGGER = LoggerFactory.getLogger(MsgChannelHandler.class);

    /**
     * 连接上服务器
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("【handlerAdded】====>" + ctx.channel().id());
        GlobalUserUtil.channels.add(ctx.channel());
    }

    /**
     * 断开连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("【handlerRemoved】====>" + ctx.channel().id());
        GlobalUserUtil.channels.remove(ctx);
    }

    /**
     * 连接异常   需要关闭相关资源
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("【系统异常】======>" + cause.toString());
        ctx.close();
        ctx.channel().close();
    }

    /**
     * 活跃的通道  也可以当作用户连接上客户端进行使用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("【channelActive】=====>" + ctx.channel());
    }

    /**
     * 不活跃的通道  就说明用户失去连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * 这里只要完成 flush
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 这里是保持服务器与客户端长连接  进行心跳检测 避免连接断开
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        LOGGER.info("userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            PingWebSocketFrame ping = new PingWebSocketFrame();
            switch (stateEvent.state()) {
                //读空闲（服务器端）
                case READER_IDLE:
                    LOGGER.info("【" + ctx.channel().remoteAddress() + "】读空闲（服务器端）");
                    ctx.writeAndFlush(ping);
                    break;
                //写空闲（客户端）
                case WRITER_IDLE:
                    LOGGER.info("【" + ctx.channel().remoteAddress() + "】写空闲（客户端）");
                    ctx.writeAndFlush(ping);
                    break;
                case ALL_IDLE:
                    LOGGER.info("【" + ctx.channel().remoteAddress() + "】读写空闲");
                    break;
            }
        }
    }

    /**
     * 收发消息处理
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info(ctx.channel().id() + " channelRead0:" + msg);

        try {
            final NetMsgHeader msgXp = new NetMsgHeader();
            final InputStream socketInput = new ByteBufInputStream((ByteBuf) msg);
            boolean ret = msgXp.decode(socketInput);
            IOUtils.closeQuietly(socketInput);

            if (!ret) return;

            LOGGER.info(String.format("client req, cmdId=%d, seq=%d", msgXp.cmdId, msgXp.seq));

            switch (msgXp.cmdId) {
                case NetMsgHeader.CMDID_NOOPING://心跳
                    byte[] respBuf = msgXp.encode();
                    LOGGER.info("心跳");
//                    LOGGER.info(String.format("client resp, cmdId=%d, seq=%d, resp.len=%d", msgXp.cmdId, msgXp.seq, msgXp.body == null ? 0 : msgXp.body.length));
                    ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(respBuf));
                    break;

                default:
                    if (msgXp.body.length > 0) {
                        LOGGER.info(new String(msgXp.body));
                        msgXp.body = "resp ok".getBytes();
                    }
                    ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(msgXp.encode()));
                    break;
            }
        } catch (Exception e) {

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }


}