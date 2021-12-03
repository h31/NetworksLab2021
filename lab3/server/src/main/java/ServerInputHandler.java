import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServerInputHandler extends ChannelInboundHandlerAdapter {

    static final List<ClientChannel> channels = new ArrayList<ClientChannel>();

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Client joined - " + ctx);
        channels.add(new ClientChannel(ctx.channel()));
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf in = (ByteBuf) msg;
        StringBuilder sb = new StringBuilder();
        while (in.isReadable()) {
            sb.append((char) in.readByte());
            System.out.println(sb.length());
        }
        ExchangeFormat clientRequest = Tool.parseRequest(sb.toString());

        System.out.println(clientRequest.toParcel());

        acceptNickname(clientRequest.getUsername(), ctx);

    }


    private void acceptNickname(String desiredNickname, ChannelHandlerContext ctx) {
        ExchangeFormat responseException = new ExchangeFormat();

        for (ClientChannel c : channels) {
            if (c.getNickname().equals(desiredNickname)) {
                responseException.setParcelType(Tool.RequestType.EXCEPTION);
                responseException.setMessage("1");
                responseException.setTime(Tool.getCurrentTime());
                getCurrentClientChannel().getChannel()
                        .writeAndFlush(getByteBufParcel(responseException));
                return;
            }
        }
        channels.get(channels.size() - 1).setNickname(desiredNickname); // last client == current client

        ExchangeFormat response = new ExchangeFormat();
        response.setParcelType(Tool.RequestType.GREETING);
        response.setUsername(desiredNickname);
        response.setTime(Tool.getCurrentTime());

        broadcastMessage(response);

    }

    private ClientChannel getCurrentClientChannel() {
        return channels.get(channels.size() - 1);
    }

    private ByteBuf getByteBufParcel(ExchangeFormat response) {
        return Unpooled.copiedBuffer(response.toParcel(), CharsetUtil.UTF_8);
    }

    private void broadcastMessage(ExchangeFormat response) {
        for (ClientChannel c : channels) {
            c.getChannel().write(getByteBufParcel(response));
            c.getChannel().flush();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
