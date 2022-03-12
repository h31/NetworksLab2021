package org.example.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.example.netty.RequestData;

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
        if (msg.isFileAttach()) {
            out.writeInt(msg.getContentLength());
            out.writeInt(msg.getAttName().length());
            out.writeCharSequence(msg.getAttName(), charset);
            out.writeBytes(msg.getContent());
        }
    }
}