package org.example.netty;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client Start");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ResponseData requestData = (ResponseData) msg;
        System.out.println("(" + requestData.getTime() + ")" + " [" + requestData.getNickname() + "] " + requestData.getText());
    }
}