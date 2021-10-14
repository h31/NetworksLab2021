package com.poly.server.threads;

import com.poly.sockets.MessageWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerThread extends Thread {

    private List<MessageWriter> writers;
    private ServerSocket serverSocket;

    private static Logger LOG = LoggerFactory.getLogger(ServerThread.class);
    private static Integer PORT = 65432;

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
                LOG.info("New client has connected");
                ClientThread clientThread = new ClientThread(clientSocket.getInputStream(), clientSocket.getOutputStream(), writers);
                clientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
