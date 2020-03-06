package com.jlin.nettydemo.netty.listener;


/**
 * @author JLin
 * @date 2020/3/4
 * @describe netty客户端监听
 */
public interface NettyClientListener {

    /**
     * 当接收到系统消息
     *
     * @param msg   消息
     */
    void onMessageResponseClient(String msg);

    /**
     * 当服务状态发生变化时触发
     *
     * @param statusCode 状态变化
     */
    void onClientStatusConnectChanged(int statusCode);
}
