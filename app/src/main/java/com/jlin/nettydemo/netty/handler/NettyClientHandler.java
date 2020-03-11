package com.jlin.nettydemo.netty.handler;

import android.util.Log;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author JLin
 * @date 2020/3/4
 * @describe netty客户端线程
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<String> {
    private static final String TAG = "NettyClientHandler";

    private Object heartBeatData;
    private boolean isHeartBeatOpen;

    public NettyClientHandler(boolean isHeartBeatOpen, Object heartBeatData) {
        this.isHeartBeatOpen = isHeartBeatOpen;
        this.heartBeatData = heartBeatData;
    }

    /**
     * 连接成功触发channelActive
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.d(TAG, "channelActive: 连接成功");
    }

    /**
     * 断开连接触发channelInactive
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.e(TAG, "channelInactive: 断开连接");
    }

    /**
     * 利用写空闲发送心跳检测消息
     *
     * @param ctx ChannelHandlerContext
     * @param evt evt
     * @throws Exception e
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {   //发送心跳
                if (isHeartBeatOpen && heartBeatData != null) {
                    if (heartBeatData instanceof String) {
                        ctx.channel().writeAndFlush(heartBeatData);
                    } else if (heartBeatData instanceof byte[]) {
                        ByteBuf buf = Unpooled.copiedBuffer((byte[]) heartBeatData);
                        ctx.channel().writeAndFlush(buf);
                    } else {
                        Log.e(TAG, "数据类型有误");
                    }
                } else {
                    Log.e(TAG, "不发送心跳");
                }
            }
        }
    }

    /**
     * 客户端收到消息
     *
     * @param ctx ChannelHandlerContext
     * @param msg 内容
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Log.d(TAG, "receive text message " + msg);
    }

    /**
     * 异常回调,默认的exceptionCaught只会打出日志，不会关掉channel
     *
     * @param ctx   ChannelHandlerContext
     * @param cause 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.e(TAG, "exceptionCaught");
        cause.printStackTrace();
        ctx.close();
    }
}
