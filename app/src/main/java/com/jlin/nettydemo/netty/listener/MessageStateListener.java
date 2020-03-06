package com.jlin.nettydemo.netty.listener;


/**
 * @author JLin
 * @date 2020/3/4
 * @describe netty客户端发送状态监听
 */
public interface MessageStateListener {
     /**
      * 是否发送成功
      * @param isSuccess isSuccess
      */
     void isSendSuccess(boolean isSuccess);
}
