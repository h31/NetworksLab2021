package org.example.server;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server {
    protected static List<MultiServer> serverList = new ArrayList<>();
    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        while (true) {
            Socket socket = serverSocket.accept();
            serverList.add(new MultiServer(socket));
        }
    }


    static class MultiServer extends Thread {

        private final Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;


        public MultiServer(Socket socket) {
            this.clientSocket = socket;
            super.start();
        }

        private void send(String msg) {
            out.write(msg + "\n");
            out.flush();

        }

        private void downService() {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                    in.close();
                    out.close();
                    for (MultiServer vr : serverList) {
                        if (vr.equals(this)) vr.interrupt();
                        serverList.remove(this);
                    }
                }
            } catch (IOException ignored) {
            }
        }

        @SneakyThrows
        @Override
        public void run() {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String word;
            try {
                word = in.readLine();
                out.write("Hello " + word + "\n");
                out.flush();
                try {
                    while ((word = in.readLine()) != null) {
                        if (word.equals("stop")) {
                            this.downService();
                            break;
                        }
                        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        String completeWord = "(" + time + ")" + word;

                        for (MultiServer vr : serverList) {
                            vr.send(completeWord);
                        }
                    }
                } catch (NullPointerException ignored) {
                }


            } catch (IOException e) {
                this.downService();
            }
        }

    }

}