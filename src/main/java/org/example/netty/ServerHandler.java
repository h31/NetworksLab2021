package org.example.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        System.out.println("Server received - " + msg);
        for (Channel c : channels) {
            RequestData requestData = (RequestData) msg;
            ResponseData responseData = new ResponseData();
            responseData.setText(requestData.getText());
            responseData.setNickname(((RequestData) msg).getNickName());
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            responseData.setTime(time);
            c.writeAndFlush(responseData);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Closing connection for client - " + ctx);
        ctx.close();
    }
}
