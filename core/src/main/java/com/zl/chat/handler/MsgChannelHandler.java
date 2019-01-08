package com.zl.chat.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zl.chat.GlobalUserUtil;
import com.zl.chat.msg.BaseMsgBody;
import com.zl.chat.msg.MsgConstant;
import com.zl.chat.msg.MsgHeader;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class MsgChannelHandler extends SimpleChannelInboundHandler<MsgHeader> {


    private static final Logger LOGGER = LoggerFactory.getLogger(MsgChannelHandler.class);

    private ObjectMapper jsonHandler = new ObjectMapper();

    public MsgChannelHandler() {
        jsonHandler.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

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
        GlobalUserUtil.channels.remove(ctx.channel());

        Set<Map.Entry<String, Channel>> entrySet = GlobalUserUtil.accountChannel.entrySet();
        for (Map.Entry<String, Channel> entry : entrySet) {
            if (entry.getValue() == ctx.channel()) {
                GlobalUserUtil.accountChannel.remove(entry.getKey());
                break;
            }
        }
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
        ctx.fireUserEventTriggered(evt);
    }

    /**
     * 收发消息处理
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, MsgHeader msg) throws Exception {
        LOGGER.info(ctx.channel().id() + " channelRead0:" + msg);

        LOGGER.info(String.format("client req, cmdId=%d, seq=%d", msg.cmdId, msg.seq));

        switch (msg.cmdId) {
            case MsgConstant.CMDID_NOOPING://心跳
                LOGGER.info("心跳");
//                    LOGGER.info(String.format("client resp, cmdId=%d, seq=%d, resp.len=%d", msgXp.cmdId, msgXp.seq, msgXp.body == null ? 0 : msgXp.body.length));
                ctx.writeAndFlush(msg);
                break;

            case MsgConstant.CMDID_LINK_ACCOUNT_CHANNEL:
                String accountId = new String(msg.body);
                LOGGER.info("关联" + accountId);
                GlobalUserUtil.accountChannel.put(accountId, ctx.channel());
                msg.body = ctx.channel().id().asShortText().getBytes();
                ctx.writeAndFlush(msg);
                break;

            case MsgConstant.CMDID_SEND_SINGLE_TEXT_MSG:

                handleSingleTextMsg(ctx, msg);
                break;
        }

    }

    private void handleSingleTextMsg(ChannelHandlerContext ctx, MsgHeader msg) {

        try {
            BaseMsgBody body = jsonHandler.readValue(msg.body, BaseMsgBody.class);

            if (GlobalUserUtil.accountChannel.get(body.getFrom()) == null) {
                GlobalUserUtil.accountChannel.put(body.getFrom(), ctx.channel());
                LOGGER.info("关联" + body.getFrom());
            }

            MsgHeader resp = new MsgHeader();
            resp.cmdId = MsgConstant.CMDID_RECEIVE_SINGLE_TEXT_MSG;
            resp.body = msg.body;
            String to = body.getTo();
            switch (to) {
                case "all":
                    GlobalUserUtil.channels.writeAndFlush(resp);
                    break;
                default:
                    Channel channel = GlobalUserUtil.accountChannel.get(to);
                    channel.writeAndFlush(resp);
                    break;
            }

        } catch (Exception e) {

            LOGGER.error(e.getMessage());
        }
        msg.body = null;
        ctx.writeAndFlush(msg);

    }

}