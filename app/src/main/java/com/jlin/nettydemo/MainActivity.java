package com.jlin.nettydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.jlin.nettydemo.netty.NettyManager;

/**
 * @author JLin
 * @date 2020/3/5
 * @describe 主界面
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String HOST = "192.168.0.137";
    public static int PORT = 6666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvConnect = findViewById(R.id.tv_connect);
        TextView tvClose = findViewById(R.id.tv_close);
        TextView tvSend = findViewById(R.id.tv_send);

        NettyManager.getInstance().initNetty();

        tvSend.setOnClickListener(view -> {

        });
    }
}

