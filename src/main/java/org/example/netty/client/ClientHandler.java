package org.example.netty.client;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.netty.ResponseData;

import static org.example.netty.Util.saveFileToClient;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client Start");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ResponseData responseData = (ResponseData) msg;
        System.out.println("(" + responseData.getTime() + ")"
                + " [" + responseData.getNickname() + "] " + responseData.getText());
        if (responseData.isFileAttach()) {
            saveFileToClient(responseData);
        }
    }
}