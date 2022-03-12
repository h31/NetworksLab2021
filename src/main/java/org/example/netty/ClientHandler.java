package org.example.netty;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client Start");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ResponseData requestData = (ResponseData) msg;
        System.out.println("(" + requestData.getTime() + ")"
                + " [" + requestData.getNickname() + "] " + requestData.getText());
        if (requestData.isFileAttach()) {
            byte[] content = requestData.getContent();
            String[] f = requestData.getAttName().split("\\.");
            StringBuilder suffix = new StringBuilder();
            for (int i = 1; i < f.length; i++) {
                suffix.append(".").append(f[i]);
            }
            try (BufferedOutputStream fileReader =
                         new BufferedOutputStream(new FileOutputStream(File.createTempFile(f[0], suffix.toString())))) {
                File file = File.createTempFile(f[0], suffix.toString());
                fileReader.write(content);
                fileReader.flush();
                System.out.println(file.getAbsolutePath());
            } catch (IOException ex) {
                log.error("error" + ex);
            }

        }
    }
}