package org.example.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RequestDataEncoder extends MessageToByteEncoder<RequestData> {

    private static final Charset charset = StandardCharsets.UTF_8;

    @Override
    protected void encode(ChannelHandlerContext ctx, RequestData msg, ByteBuf out) {
        out.writeInt(msg.getText().length());
        out.writeCharSequence(msg.getText(), charset);
        out.writeInt(msg.getNickName().length());
        out.writeCharSequence(msg.getNickName(), charset);
        out.writeBoolean(msg.isFileAttach());
        if (msg.getContentLength() != 0) {
            out.writeInt(msg.getContentLength());
            out.writeBytes(msg.getContent());
        }
    }
}