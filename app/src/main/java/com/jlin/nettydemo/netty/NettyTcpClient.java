package com.jlin.nettydemo.netty;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.jlin.nettydemo.netty.handler.NettyClientHandler;
import com.jlin.nettydemo.netty.listener.MessageStateListener;
import com.jlin.nettydemo.netty.listener.NettyClientListener;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

/**
 * @author JLin
 * @date 2020/3/4
 * @describe netty客户端
 */
public class NettyTcpClient {
    private static final String TAG = "NettyTcpClient";

    /**
     * 主机ip
     */
    private String host;
    /**
     * 主机port
     */
    private int port;

    private EventLoopGroup group;
    private Channel channel;

    /**
     * 最大重连次数
     */
    private int maxConnectTimes;
    private int reconnectNum = maxConnectTimes;
    private boolean isConnect = false;
    private boolean isConnecting = false;

    /**
     * 重连间隔时间
     */
    private long reconnectIntervalTime = 5000;

    /**
     * 心跳间隔时间
     */
    private int heartBeatInterval = 5;//单位秒

    /**
     * 是否发送心跳
     */
    private boolean isHeartBeatOpen = false;

    /**
     * 数据间隔 默认为换行转义字符
     */
    private String packetSeparator;

    /**
     * 状态和消息监听
     */
    private NettyClientListener nettyClientListener;

    private NettyTcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        if (isConnecting) {
            return;
        }
        Log.d(TAG, "正在尝试连接...");
        isConnecting = true;
        group = new NioEventLoopGroup();
        new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(group)
                .option(ChannelOption.TCP_NODELAY, true) // 不延迟，直接发送
                .option(ChannelOption.SO_KEEPALIVE, true) // 保持长连接状态
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, heartBeatInterval, 0));
                        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
//                        pipeline.addLast("encoder", new NettyEncoder());
//                        pipeline.addLast("decoder", new NettyDecoder());
                        pipeline.addLast(new NettyClientHandler(isHeartBeatOpen, nettyClientListener));
                    }
                })
                .connect(new InetSocketAddress(host, port))
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        // 连接成功
                        channel = future.channel();
                        isConnect = true;
                        isConnecting = false;
                        reconnectNum = maxConnectTimes;
                    } else {
                        // 这里一定要关闭，不然一直重试会引发OOM
                        isConnect = false;
                        future.channel().close();
                        group.shutdownGracefully();
                        isConnecting = false;
                        reconnect();
                    }
                });
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        Log.d(TAG, "断开连接...");
        group.shutdownGracefully();
    }

    /**
     * 重新连接
     */
    public void reconnect() {
        if (reconnectNum > 0 && !isConnect) {
            Log.d(TAG, "正在尝试第" + (maxConnectTimes - reconnectNum + 1) + "重新连接...");
            reconnectNum--;
            SystemClock.sleep(reconnectIntervalTime);
            connect();
        }
    }

    /**
     * 异步发送
     *
     * @param data     要发送的数据
     * @param listener 发送结果回调
     * @return 方法执行结果
     */
    public boolean sendMsgToServer(String data, final MessageStateListener listener) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            String separator = TextUtils.isEmpty(packetSeparator) ? System.getProperty("line.separator") : packetSeparator;
            channel.writeAndFlush(data + separator).addListener((ChannelFutureListener) channelFuture1 ->
                    listener.isSendSuccess(channelFuture1.isSuccess()));
        }
        return flag;
    }

    /**
     * 同步发送
     *
     * @param data 要发送的数据
     */
    public void sendMsgToServer(String data) {
        try {
            String separator = TextUtils.isEmpty(packetSeparator) ? System.getProperty("line.separator") : packetSeparator;
            channel.writeAndFlush(data + separator).addListener((ChannelFutureListener) future -> {
                Log.d(TAG, "发送消息 ------- >: " + data + " ---- " + future.isSuccess());
            });
            channel.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步发送
     *
     * @param data     要发送的数据
     * @param listener 发送结果回调
     * @return 方法执行结果
     */
    public boolean sendMsgToServerWithNoSeparator(String data, final MessageStateListener listener) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            channel.writeAndFlush(data).addListener((ChannelFutureListener) channelFuture1 ->
                    listener.isSendSuccess(channelFuture1.isSuccess()));
        }
        return flag;
    }

    /**
     * 同步发送
     *
     * @param data 要发送的数据
     */
    public void sendMsgToServerWithNoSeparator(String data) {
        try {
            channel.writeAndFlush(data).addListener((ChannelFutureListener) future -> {
                Log.d(TAG, "发送消息 ------- >: " + data + " ---- " + future.isSuccess());
            });
            channel.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送byte
     *
     * @param data     data
     * @param listener listener
     * @return flag
     */
    public boolean sendMsgToServer(byte[] data, final MessageStateListener listener) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            ByteBuf buf = Unpooled.copiedBuffer(data);
            channel.writeAndFlush(buf).addListener((ChannelFutureListener) channelFuture ->
                    listener.isSendSuccess(channelFuture.isSuccess()));
        }
        return flag;
    }

    /**
     * 获取TCP连接状态
     *
     * @return 获取TCP连接状态
     */
    public boolean getConnectStatus() {
        return isConnect;
    }

    /**
     * 判断是否在连接中
     *
     * @return b
     */
    public boolean isConnecting() {
        return isConnecting;
    }

    public byte[] strToByteArray(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes();
    }

    public void setNettyClientListener(NettyClientListener nettyClientListener) {
        this.nettyClientListener = nettyClientListener;
    }

    /**
     * 构建者，创建NettyTcpClient
     */
    public static class Builder {
        /**
         * 服务器地址
         */
        private String host;
        /**
         * 服务器端口
         */
        private int port;

        /**
         * 最大重连次数
         */
        private int maxConnectTimes = Integer.MAX_VALUE;

        /**
         * 重连间隔
         */
        private long reconnectIntervalTime = 5000;

        /**
         * 是否发送心跳
         */
        private boolean isHeartBeatOpen;
        /**
         * 心跳时间间隔
         */
        private int heartBeatInterval = 5;

        /**
         * 间隔符
         */
        private String packetSeparator;

        public Builder setMaxReconnectTimes(int reConnectTimes) {
            this.maxConnectTimes = reConnectTimes;
            return this;
        }

        public Builder setReconnectIntervalTime(long reconnectIntervalTime) {
            this.reconnectIntervalTime = reconnectIntervalTime;
            return this;
        }

        public Builder setHostAndPort(String host, int port) {
            this.host = host;
            this.port = port;
            return this;
        }

        public Builder setHeartBeatInterval(int intervalTime) {
            this.heartBeatInterval = intervalTime;
            return this;
        }

        public Builder setHeartBeatOpen(boolean heartBeatOpen) {
            isHeartBeatOpen = heartBeatOpen;
            return this;
        }

        public Builder setPacketSeparator(String packetSeparator) {
            this.packetSeparator = packetSeparator;
            return this;
        }

        public NettyTcpClient build() {
            NettyTcpClient nettyTcpClient = new NettyTcpClient(host, port);
            nettyTcpClient.maxConnectTimes = this.maxConnectTimes;
            nettyTcpClient.reconnectNum = this.maxConnectTimes;
            nettyTcpClient.reconnectIntervalTime = this.reconnectIntervalTime;
            nettyTcpClient.heartBeatInterval = this.heartBeatInterval;
            nettyTcpClient.isHeartBeatOpen = this.isHeartBeatOpen;
            nettyTcpClient.packetSeparator = this.packetSeparator;
            return nettyTcpClient;
        }
    }
}
