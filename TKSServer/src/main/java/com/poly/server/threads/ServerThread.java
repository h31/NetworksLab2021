package com.poly.server.threads;

import com.poly.sockets.MessageWriter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerThread extends Thread {

    private static Integer PORT = 65432;
    private List<MessageWriter> writers;

    private ServerSocket serverSocket;

    public ServerThread() throws IOException {
        this.serverSocket = new ServerSocket(PORT);
        this.writers = new LinkedList<>();
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(clientSocket.getInputStream(), clientSocket.getOutputStream(), writers);
                clientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
