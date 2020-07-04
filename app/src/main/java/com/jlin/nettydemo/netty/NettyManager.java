package com.jlin.nettydemo.netty;

import android.util.Log;

import com.jlin.nettydemo.netty.listener.NettyClientListener;
import com.jlin.nettydemo.netty.status.ConnectState;

import org.greenrobot.eventbus.EventBus;

public class NettyManager {
    private static final String TAG = "NettyManager";
    private static NettyManager mInstance = null;

    public static final String HOST = "192.168.42.5";
    public static int PORT = 8080;

    private NettyTcpClient nettyTcpClient;

    public static NettyManager getInstance() {
        synchronized (NettyManager.class) {
            if (mInstance == null) {
                mInstance = new NettyManager();
            }
        }
        return mInstance;
    }

    public void initNetty() {
        nettyTcpClient = new NettyTcpClient.Builder()
                .setHostAndPort(HOST, PORT)    //设置服务端地址和端口
                .setMaxReconnectTimes(5)    //设置最大重连次数
                .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                .setHeartBeatOpen(true) //设置是否发送心跳
                .setHeartBeatInterval(15)    //设置心跳间隔时间。单位：秒
                .build();

        nettyTcpClient.setNettyClientListener(new NettyClientListener() {
            @Override
            public void onMessageResponseClient(String msg) {
                EventBus.getDefault().post(new NettyEvent("NETTY_MESSAGE", msg));
            }

            @Override
            public void onClientStatusConnectChanged(int statusCode) {
                switch (statusCode) {
                    case ConnectState.STATUS_CONNECT_SUCCESS:
                        Log.d(TAG, "onClientStatusConnectChanged: 连接成功");
                        break;
                    case ConnectState.STATUS_CONNECT_CLOSED:
                        Log.e(TAG, "onClientStatusConnectChanged: 连接关闭");
                        // todo 重新发起重连
                        initNetty();
                        break;
                    case ConnectState.STATUS_CONNECT_ERROR:
                        Log.e(TAG, "onClientStatusConnectChanged: 连接失败");
                        break;
                    default:
                        break;
                }
            }
        });
        nettyTcpClient.connect();
    }

    public void sentMessage(String data){
        nettyTcpClient.sendMsgToServer(data);
    }
}
