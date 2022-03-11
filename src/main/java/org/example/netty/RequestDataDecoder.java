package org.example.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RequestDataDecoder extends ReplayingDecoder<RequestData> {

    private static final Charset charset = StandardCharsets.UTF_8;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IOException {
        RequestData data = new RequestData();
        int textLength = in.readInt();
        data.setText(in.readCharSequence(textLength, charset).toString());
        int strLen = in.readInt();
        data.setNickName(in.readCharSequence(strLen, charset).toString());
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