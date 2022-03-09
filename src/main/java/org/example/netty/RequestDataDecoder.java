package org.example.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RequestDataDecoder extends ReplayingDecoder<RequestData> {

    private static final Charset charset = StandardCharsets.UTF_8;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        RequestData data = new RequestData();
        int textLength = in.readInt();
        data.setText(in.readCharSequence(textLength, charset).toString());
        int strLen = in.readInt();
        data.setNickName(in.readCharSequence(strLen, charset).toString());
        out.add(data);
    }
}