import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    boolean isReadComplete = false;
    ExchangeFormat parcel;
    StringBuilder sb;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] bufferByteArray;
    ByteArrayOutputStream stringByteArray = new ByteArrayOutputStream();
    ByteBuf stringByteBuf;


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (isReadComplete) {
            if (in.readableBytes() < 1) {
                parcel.setAttachmentByteArray(outputStream.toByteArray());
                isReadComplete = false;
                out.add(parcel);
                outputStream.reset();
                return;
            }
            bufferByteArray = new byte[in.readableBytes()];
            in.readBytes(bufferByteArray);
            outputStream.write(bufferByteArray);

            if(outputStream.size() == parcel.getAttachmentSize()) {
                parcel.setAttachmentByteArray(outputStream.toByteArray());
                isReadComplete = false;
                out.add(parcel);
                outputStream.reset();
                return;
            }
            return;
        }


        byte c;
        while (in.isReadable()) {
            c = in.readByte();
            if (c == '\n') {
                isReadComplete = true;
                break;
            }
            stringByteArray.write(c);
        }
        if(!isReadComplete) {
            return;
        }

        stringByteBuf = Unpooled.copiedBuffer(stringByteArray.toByteArray());

        parcel = Tool.parseRequest(stringByteBuf.toString(Charset.defaultCharset()));
        if(parcel.getAttachmentSize() > 0) {
            isReadComplete = true;
            return;
        }
        isReadComplete = false;
        stringByteArray.reset();
        out.add(parcel);
        return;


    }
}
