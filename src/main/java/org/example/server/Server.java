package org.example.server;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.example.util.StreamUtil.readLine;

public class Server {
    Logger logger = Logger.getLogger(Server.class);
    protected static List<MultiServer> serverList = new ArrayList<>();

    public void start(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket socket = serverSocket.accept();
            new MultiServer(socket);
            logger.info("New user connected: " + socket);
        }
    }

    static class MultiServer extends Thread {

        private final Socket clientSocket;
        private BufferedInputStream in;
        private BufferedOutputStream out;
        private BufferedWriter writer;
        Logger logger = Logger.getLogger(MultiServer.class);
        private String nickname;

        public MultiServer(Socket socket) {
            this.clientSocket = socket;
            super.start();
        }

        private void send(String message, byte[] content) throws IOException {
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.write(content);
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
                    writer.close();
                    in.close();
                    out.close();
                    for (MultiServer vr : serverList) {
                        if (vr.equals(this)) vr.interrupt();
                        serverList.remove(this);
                        logger.info("user disconnect: " + this.nickname);
                    }
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }

        @Override
        public void run() {
            try {
                in = new BufferedInputStream(clientSocket.getInputStream());
                out = new BufferedOutputStream(clientSocket.getOutputStream());
                writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            } catch (IOException e) {
                logger.error(e);
            }
            String line;
            String text = "";
            String attName = "";
            int attSize = 0;
            try {
                nickname = readLine(in);
                serverList.add(this);
                logger.info("registry new username: " + nickname);
                writer.write("Hello " + nickname + "\n");
                writer.flush();
                while (clientSocket.isConnected()) {
                    for (int i = 0; i < 4; i++) {
                        line = readLine(in);
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
                    } else {
                        String message = time + "\n" + nickname + "\n" + text + "\n" + attName + "\n" + 0 + "\n";

                        for (MultiServer vr : serverList) {
                            vr.sendMessage(message);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error(e);
                this.downService();
            }
        }

    }

}