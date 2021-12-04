package com.poly.server;

import com.poly.server.thread.ServerThread;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerThread serverThread = new ServerThread(11122);
        serverThread.run();
    }
}
