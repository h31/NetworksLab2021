package org.example.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ResponseDataEncoder extends MessageToByteEncoder<ResponseData> {
    private static final Charset charset = StandardCharsets.UTF_8;

    @Override
    protected void encode(ChannelHandlerContext ctx, ResponseData msg, ByteBuf out) {
        out.writeInt(msg.getText().length());
        out.writeCharSequence(msg.getText(), charset);
        out.writeInt(msg.getNickname().length());
        out.writeCharSequence(msg.getNickname(), charset);
        out.writeInt(msg.getTime().length());
        out.writeCharSequence(msg.getTime(), charset);
    }
}