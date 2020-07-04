package com.jlin.nettydemo.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<String> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String data, ByteBuf out) {
        out.writeBytes(stringToBytes(data));
    }

    private static byte[] stringToBytes(String s) {
        if (s.length() % 2 != 0) {
            return null;
        }
        byte[] requestData = new byte[128];
        int len = 0;
        do {
            String substring = s.substring(len, (len + 2));
            int i = Integer.parseInt(substring, 16);
            requestData[len / 2] = (byte) (i & 0xff);
            len += 2;
        } while ((len) != s.length());
        byte[] rtn = new byte[len / 2];
        System.arraycopy(requestData, 0, rtn, 0, len / 2);
        return rtn;
    }
}
