package org.example.client;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.util.StreamUtil.readLine;

public class Client {

    Logger logger = Logger.getLogger(Client.class);

    private Socket socket;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private BufferedWriter writer;
    private BufferedReader inputUser;
    private String nickname;
    private final Pattern pattern = Pattern.compile(" ?-a (.*)$");
    private final String STOP_WORD = "stop";

    public void startConnection(String ip, int port) {
        try {
            this.socket = new Socket(ip, port);
            logger.info("Socket connection");
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            writer = new BufferedWriter(new OutputStreamWriter(out));
            this.pressNickname();
            new ReadMsg().start();
            new WriteMsg().start();
        } catch (IOException e) {
            logger.error("Socket failed");
            downService();
        }
    }

    private void pressNickname() throws IOException {
        System.out.print("Enter your nickname: ");
        nickname = inputUser.readLine();
        writer.write(nickname + "\n");
        writer.flush();
    }

    private void downService() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                writer.close();
                in.close();
                out.close();
            }
        } catch (IOException ex) {
            logger.error("something went wrong", ex);
        }
    }

    private class ReadMsg extends Thread {
        @Override
        public void run() {
            String line;
            String text = "";
            String attName = "";
            String time = "";
            int attSize = 0;
            try {
                System.out.println(readLine(in));
                while (socket.isConnected()) {
                    for (int i = 0; i < 5; i++) {
                        line = readLine(in);
                        if (line.equals(STOP_WORD)) {
                            downService();
                            break;
                        }
                        switch (i) {
                            case 0:
                                time = line;
                                break;
                            case 1:
                                nickname = line;
                                break;
                            case 2:
                                text = line;
                                break;
                            case 3:
                                attName = line;
                                break;
                            case 4:
                                attSize = NumberUtils.isNumber(line) ? Integer.parseInt(line) : 0;
                                break;
                        }
                    }
                    String message = "(" + time + ")" + " [" + nickname + "] " + text;
                    System.out.println(message);
                    if (attSize != 0) {
                        byte[] content = new byte[attSize];
                        in.readNBytes(content, 0, attSize);
                        String[] f = attName.split("\\.");
                        StringBuilder suffix = new StringBuilder();
                        for (int i = 1; i < f.length; i++) {
                            suffix.append(".").append(f[i]);
                        }
                        File file = File.createTempFile(f[0], suffix.toString());
                        BufferedOutputStream fileReader = new BufferedOutputStream(new FileOutputStream(file));
                        fileReader.write(content);
                        fileReader.flush();
                        fileReader.close();
                        System.out.println(file.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                downService();
            }
        }
    }

    public class WriteMsg extends Thread {

        @Override
        public void run() {
            while (!socket.isClosed()) {
                String text;
                Matcher matcher;
                try {
                    text = inputUser.readLine();
                    if (text.isBlank()) continue;
                    if (text.equals(STOP_WORD)) {
                        writer.write(STOP_WORD + "\n");
                        downService();
                        break;
                    }
                    matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        String path = matcher.group(1);
                        File file = new File(path);
                        if (file.isFile()) {
                            text = matcher.replaceFirst("(" + file.getName() + " attached)");
                            BufferedInputStream fileReader = new BufferedInputStream(new FileInputStream(file));
                            byte[] content = fileReader.readAllBytes();
                            fileReader.close();
                            writer.write((nickname + "\n" + text + "\n" + file.getName() + "\n"
                                    + content.length + "\n"));
                            writer.flush();
                            out.write(content);
                            out.flush();
                        }
                    } else {
                        writer.write(nickname + "\n" + text + "\n" + "\n" + "\n");
                        writer.flush();
                    }

                } catch (IOException e) {
                    downService();
                }
            }
        }
    }
}
