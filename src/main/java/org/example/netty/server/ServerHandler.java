package org.example.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.netty.RequestData;
import org.example.netty.ResponseData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    static final List<Channel> channels = new ArrayList<>();

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Client joined - " + ctx);
        channels.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        for (Channel c : channels) {
            RequestData requestData = (RequestData) msg;
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            ResponseData responseData = ResponseData.builder()
                    .text(requestData.getText())
                    .nickname(requestData.getNickName())
                    .time(time)
                    .fileAttach(requestData.isFileAttach())
                    .contentLength(requestData.getContentLength())
                    .attName(requestData.getAttName())
                    .content(requestData.getContent())
                    .build();
            c.writeAndFlush(responseData);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Closing connection for client - " + ctx);
        ctx.close();
    }
}
