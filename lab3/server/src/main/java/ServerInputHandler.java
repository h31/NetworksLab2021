import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServerInputHandler extends ChannelInboundHandlerAdapter {

    static final List<Channel> channels = new ArrayList<Channel>();

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Client joined - " + ctx);
        channels.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        StringBuilder sb = new StringBuilder();
        while (byteBuf.isReadable()) {
            sb.append((char)byteBuf.readByte());
        }
        acceptNickname();
       /* System.out.println(msg);
        if(msg.toString().contains("greeting")) {
            ExchangeFormat response = new ExchangeFormat();
            response.setParcelType(Tool.RequestType.GREETING);
            response.setUsername("jorik");
            response.setTime(Tool.getCurrentTime());
            for (Channel c : channels) {
                c.writeAndFlush(msg);
                //c.writeAndFlush(response.toParcel());
            }
        }*/
        ctx.write("lol!!!!!!!");
        System.out.println(sb);
        byteBuf.release();
    }


    private void acceptNickname() {
        ExchangeFormat response = new ExchangeFormat();
        response.setParcelType(Tool.RequestType.GREETING);
        response.setUsername("jorik");
        response.setTime(Tool.getCurrentTime());
        for (Channel c : channels) {
            System.out.println(c.remoteAddress());
            c.writeAndFlush(Unpooled.copiedBuffer(response.toParcel(), StandardCharsets.UTF_8));
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
