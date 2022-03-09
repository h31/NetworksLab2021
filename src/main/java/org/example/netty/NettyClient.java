package org.example.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

public class NettyClient {

    private static final String STOP_WORD = "stop";

    public static void main(String[] args) throws Exception {
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
                if (text.equals(STOP_WORD)) {
                    workerGroup.shutdownGracefully();
                    break;
                }
                RequestData msg = new RequestData();
                msg.setText(text);
                msg.setNickName(clientName);
                channel.writeAndFlush(msg);
                channel.flush();
            }
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
