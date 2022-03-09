package org.example.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ResponseDataDecoder extends ReplayingDecoder<ResponseData> {
    private static final Charset charset = StandardCharsets.UTF_8;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        ResponseData data = new ResponseData();
        int textLength = in.readInt();
        data.setText(in.readCharSequence(textLength, charset).toString());
        int nicknameLength = in.readInt();
        data.setNickname(in.readCharSequence(nicknameLength, charset).toString());
        int timeLength = in.readInt();
        data.setTime(in.readCharSequence(timeLength, charset).toString());
        out.add(data);
    }
}