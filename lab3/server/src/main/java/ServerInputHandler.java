import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerInputHandler extends ChannelInboundHandlerAdapter {

    static final List<ClientChannel> channels = new ArrayList<ClientChannel>();
    private String clientNickname = "";

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Client joined - " + ctx);
        channels.add(new ClientChannel(ctx.channel()));
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf in = (ByteBuf) msg;
        StringBuilder sb = new StringBuilder();
        byte c;
        while (in.isReadable()) {
            c = in.readByte();
            if (c == '\n') {
                System.out.println("а вот и новая стркоа");
                break;
            }
            sb.append((char) c);
            System.out.println(sb);
        }
        System.out.println("переходим к преобразованию посылки");
        ExchangeFormat clientRequest = Tool.parseRequest(sb.toString());


        //if null

        System.out.println("То, что я отправил клиенту = " + clientRequest.toParcel());

        //
        if (clientRequest.getParcelType() == Tool.RequestType.GREETING) {
            acceptNickname(clientRequest.getUsername());
            return;
        }
        //if exit
        if (clientRequest.getParcelType() == Tool.RequestType.EXIT) {
            channels.remove(getCurrentClientChannel());
            notifyAboutUserExit(getCurrentClientNickname());
            ctx.close();
            return;
        }

        processDefaultMessage(clientRequest, in);
    }


    private void acceptNickname(String desiredNickname) {
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
        clientNickname = desiredNickname;

        ExchangeFormat response = new ExchangeFormat();
        response.setParcelType(Tool.RequestType.GREETING);
        response.setUsername(desiredNickname);
        response.setTime(Tool.getCurrentTime());

        broadcastMessage(response);

    }

    private void processDefaultMessage(ExchangeFormat clientRequest, ByteBuf in) {
        ExchangeFormat serverResponse = new ExchangeFormat();

        serverResponse.setParcelType(Tool.RequestType.MESSAGE);
        serverResponse.setTime(Tool.getCurrentTime());
        serverResponse.setUsername(getCurrentClientNickname());
        serverResponse.setMessage(clientRequest.getMessage());

        if (clientRequest.getAttachmentSize() != 0) {
            byte[] byteArray = new byte[clientRequest.getAttachmentSize()];
            int i = 0;
            while (in.isReadable()) {
                byteArray[i] = in.readByte();
                i++;
            }

            System.out.println("а вот и итоговый байтеррей = " + Arrays.toString(byteArray));

            System.out.println("клиент вложил файл");
            serverResponse.setAttachmentName(clientRequest.getAttachmentName());
            serverResponse.setAttachmentSize(clientRequest.getAttachmentSize());
            serverResponse.setAttachmentByteArray(byteArray);
            broadcastMessageWithFile(serverResponse);
            return;
        }

        broadcastMessage(serverResponse);
    }

    private ClientChannel getCurrentClientChannel() {
        for (ClientChannel c : channels) {
            if (c.getNickname().equals(clientNickname)) return c;
        }
        return channels.get(channels.size() - 1);
    }

    private String getCurrentClientNickname() {
        return getCurrentClientChannel().getNickname();
    }

    private ByteBuf getByteBufParcel(ExchangeFormat response) {
        return Unpooled.copiedBuffer(response.toParcel(), CharsetUtil.UTF_8);
    }

    private void notifyAboutUserExit(String clientNickname) {
        ExchangeFormat notifyParcel = new ExchangeFormat();
        notifyParcel.setParcelType(Tool.RequestType.EXIT);
        notifyParcel.setUsername(clientNickname);
        notifyParcel.setTime(Tool.getCurrentTime());
        broadcastMessage(notifyParcel);
    }

    private void broadcastMessage(ExchangeFormat response) {
        for (ClientChannel c : channels) {
            c.getChannel().write(getByteBufParcel(response));
            c.getChannel().flush();
        }
    }

    private void broadcastMessageWithFile(ExchangeFormat response) {
        Channel channel;
        for (ClientChannel c : channels) {
            channel = c.getChannel();
            if (c.getNickname().equals(clientNickname)) {
                channel.write(getByteBufParcel(response));
                channel.flush();
            } else {
                channel.write(getByteBufParcel(response));
                System.out.println(response.getAttachmentByteArray().length + "длина Bytearray");
                channel.writeAndFlush(Unpooled.copiedBuffer(response.getAttachmentByteArray()));
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        channels.remove(getCurrentClientChannel());
        notifyAboutUserExit(clientNickname);
        ctx.close();
    }
}
