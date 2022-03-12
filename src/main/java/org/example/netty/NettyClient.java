package org.example.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class NettyClient {

    private static final Pattern pattern = Pattern.compile(" ?-a (.*)$");
    private static final String STOP_WORD = "stop";

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter your name: ");
        String clientName = "";
        if (scanner.hasNext()) {
            clientName = scanner.nextLine();
            System.out.println("Welcome " + clientName);
        }

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new RequestDataEncoder(), new ResponseDataDecoder(), new ClientHandler());
                }
            });

            ChannelFuture f = b.connect(host, port).sync();

            while (scanner.hasNext()) {
                String text = scanner.nextLine();
                Channel channel = f.sync().channel();
                Matcher matcher = pattern.matcher(text);
                if (text.equals(STOP_WORD)) {
                    workerGroup.shutdownGracefully();
                    break;
                }
                if (matcher.find()) {
                    String path = matcher.group(1);
                    File file = new File(path);
                    if (file.isFile()) {
                        text = matcher.replaceFirst(" (" + file.getName() + " attached)");
                        RequestData msg = new RequestData();
                        msg.setText(text);
                        msg.setNickName(clientName);
                        byte[] content;
                        try (BufferedInputStream fileReader = new BufferedInputStream(new FileInputStream(file))) {
                            content = fileReader.readAllBytes();
                            msg.setFileAttach(true);
                            msg.setContentLength(content.length);
                            msg.setAttName(file.getName());
                            msg.setContent(content);
                        } catch (IOException ex) {
                            log.error("error" + ex);
                        }
                        channel.writeAndFlush(msg);
                    }
                } else {
                    RequestData msg = new RequestData();
                    msg.setText(text);
                    msg.setNickName(clientName);
                    msg.setFileAttach(false);
                    channel.writeAndFlush(msg);
                }
            }
            f.channel().closeFuture().sync();
        } catch (InterruptedException ex) {
            log.error("error" + ex);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
