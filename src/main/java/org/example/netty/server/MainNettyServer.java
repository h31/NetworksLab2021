package org.example.netty.server;

public class MainNettyServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        new NettyServer(PORT).run();
    }
}
