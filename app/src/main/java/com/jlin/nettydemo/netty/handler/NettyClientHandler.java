package com.jlin.nettydemo.netty.handler;

import android.util.Log;

import com.jlin.nettydemo.netty.listener.NettyClientListener;
import com.jlin.nettydemo.netty.status.ConnectState;

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

    private boolean isHeartBeatOpen;
    private NettyClientListener nettyClientListener;

    public NettyClientHandler(boolean isHeartBeatOpen, NettyClientListener listener) {
        this.isHeartBeatOpen = isHeartBeatOpen;
        this.nettyClientListener = listener;
    }

    /**
     * 连接成功触发channelActive
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        nettyClientListener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_SUCCESS);
    }

    /**
     * 断开连接触发channelInactive
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        nettyClientListener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_CLOSED);
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
                if (isHeartBeatOpen) {
                    String heartBeat = getHeartBeat();
                    Log.d(TAG, "发送心跳包 ----> : " + heartBeat);
                    ctx.channel().writeAndFlush(heartBeat + System.getProperty("line.separator"));
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
        nettyClientListener.onMessageResponseClient(msg);
    }

    /**
     * 异常回调,默认的exceptionCaught只会打出日志，不会关掉channel
     *
     * @param ctx   ChannelHandlerContext
     * @param cause 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        nettyClientListener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_ERROR);
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * @return heartBeat
     */
    private String getHeartBeat() {
        return "heartBeat";
    }
}
