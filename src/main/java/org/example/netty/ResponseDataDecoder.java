package org.example.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ResponseDataDecoder extends ReplayingDecoder<ResponseData> {
    private static final Charset charset = StandardCharsets.UTF_8;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IOException {
        ResponseData data = new ResponseData();
        int textLength = in.readInt();
        data.setText(in.readCharSequence(textLength, charset).toString());
        int nicknameLength = in.readInt();
        data.setNickname(in.readCharSequence(nicknameLength, charset).toString());
        int timeLength = in.readInt();
        data.setTime(in.readCharSequence(timeLength, charset).toString());
        boolean isFileAttach = in.readBoolean();
        data.setFileAttach(isFileAttach);
        if (isFileAttach) {
            ByteArrayOutputStream stringByteArray = new ByteArrayOutputStream();
            int contentLength = in.readInt();
            data.setContentLength(contentLength);
            for (int i = 0; i < contentLength; i++) {
                byte contentByte = in.readByte();
                stringByteArray.write(contentByte);
            }
            data.setContent(stringByteArray.toByteArray());
            stringByteArray.close();
        }
        out.add(data);
    }
}