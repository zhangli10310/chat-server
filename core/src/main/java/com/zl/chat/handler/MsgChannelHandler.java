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
import org.springframework.lang.NonNull;

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

        Set<Map.Entry<String, GlobalUserUtil.UserInfo>> entrySet = GlobalUserUtil.accountChannel.entrySet();
        for (Map.Entry<String, GlobalUserUtil.UserInfo> entry : entrySet) {
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
                GlobalUserUtil.UserInfo info = linkAccount(accountId, ctx.channel());
                msg.body = info.token.getBytes();
                ctx.writeAndFlush(msg);
                break;

            case MsgConstant.CMDID_SEND_SINGLE_TEXT_MSG:

                handleSingleTextMsg(ctx, msg);
                break;
        }

    }

    /**
     * 关联ID和用户信息
     * @param id  用户ID
     * @param channel .
     */
    private GlobalUserUtil.UserInfo linkAccount(@NonNull String id, Channel channel) {
        GlobalUserUtil.UserInfo info = info = new GlobalUserUtil.UserInfo();
        info.accountId = id;
        info.channel = channel;
        info.token = generateToken(channel);
        LOGGER.info("关联" + id);
        return info;
    }

    private String generateToken(Channel channel) {
        return System.currentTimeMillis() % 10000 + "-" + channel.hashCode() % 10000;
    }

    private boolean tokenOk(String from, Channel channel) {
        return true;
    }

    private void handleSingleTextMsg(ChannelHandlerContext ctx, MsgHeader msg) {

        try {
            BaseMsgBody body = jsonHandler.readValue(msg.body, BaseMsgBody.class);

            if (!tokenOk(body.getFrom(), ctx.channel())) {
                msg.body = new byte[]{1};
                ctx.writeAndFlush(msg);
                return;
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
                    Channel channel = GlobalUserUtil.accountChannel.get(to).channel;
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