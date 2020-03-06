package com.jlin.nettydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jlin.nettydemo.bean.PersonBean;
import com.jlin.nettydemo.netty.NettyTcpClient;

/**
 * @author JLin
 * @date 2020/3/5
 * @describe 主界面
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String HOST = "192.168.0.137";
    public static int PORT = 6666;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PersonBean personBean = new PersonBean("JLin", 20);
        TextView tvConnect = findViewById(R.id.tv_connect);
        TextView tvClose = findViewById(R.id.tv_close);
        TextView tvSend = findViewById(R.id.tv_send);

        NettyTcpClient mNettyTcpClient = new NettyTcpClient.Builder()
                .setHostAndPort(HOST, PORT)    //设置服务端地址和端口
                .setMaxReconnectTimes(5)    //设置最大重连次数
                .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                .setHeartBeatOpen(false) //设置是否发送心跳
                .setHeartBeatInterval(5)    //设置心跳间隔时间。单位：秒
                .setPacketSeparator("\r\n")       // 设置间隔符
                .setHeartBeatData(new Gson().toJson(personBean)) //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
                .build();

        tvConnect.setOnClickListener(view -> mNettyTcpClient.connect());
        tvSend.setOnClickListener(view -> {
            mHandler.post(() -> {
                try {
                    mNettyTcpClient.sendMsgToServer("123456");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        tvClose.setOnClickListener(view -> mNettyTcpClient.disconnect());
    }
}

