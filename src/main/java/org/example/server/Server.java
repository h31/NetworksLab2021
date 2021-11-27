package org.example.server;

import lombok.SneakyThrows;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
        private BufferedInputStream in;
        private BufferedOutputStream out;
        private BufferedReader reader;
        private BufferedWriter writer;


        public MultiServer(Socket socket) {
            this.clientSocket = socket;
            super.start();
        }

        private void send(String message, byte[] content) throws IOException {
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.write(Arrays.copyOf(content, content.length));
            out.flush();
        }

        private void sendMessage(String message) throws IOException {
            writer.write(message);
            writer.flush();
        }

        private void downService() {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                    reader.close();
                    writer.close();
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
            in = new BufferedInputStream(clientSocket.getInputStream());
            out = new BufferedOutputStream(clientSocket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(in));
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            String line;
            String nickname = "";
            String text = "";
            String attName = "";
            int attSize = 0;
            try {
                line = reader.readLine();
                writer.write("Hello " + line + "\n");
                writer.flush();
                try {
                    while (clientSocket.isConnected()) {
                        for (int i = 0; i < 4; i++) {
                            line = reader.readLine();
                            if (line.equals("stop")) {
                                this.downService();
                                break;
                            }
                            switch (i) {
                                case 0:
                                    nickname = line;
                                    break;
                                case 1:
                                    text = line;
                                    break;
                                case 2:
                                    attName = line;
                                    break;
                                case 3:
                                    attSize = NumberUtils.isNumber(line) ? Integer.parseInt(line) : 0;
                                    break;
                            }
                        }
                        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        if (attSize != 0) {
                            byte[] content = new byte[attSize];
                            in.readNBytes(content, 0, attSize);
                            String message = time + "\n" + nickname + "\n" + text + "\n" + attName + "\n"
                                    + content.length + "\n";
                            for (MultiServer vr : serverList) {
                                vr.send(message, content);
                            }
                        }
                        else {
                            String message = time + "\n" + nickname + "\n" + text + "\n" + attName + "\n"
                                    + 0 + "\n";
                            for (MultiServer vr : serverList) {
                                vr.sendMessage(message);
                            }
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