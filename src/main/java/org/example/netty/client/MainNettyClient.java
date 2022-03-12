package org.example.netty.client;

public class MainNettyClient {
    private static final int PORT = 8080;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        new NettyClient(HOST, PORT).start();
    }
}
