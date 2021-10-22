package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader inputUser;
    private String nickName;

    public void startConnection(String ip, int port) {
        try {
            this.socket = new Socket(ip, port);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            this.pressNickname();
            new ReadMsg().start();
            new WriteMsg().start();
        } catch (IOException e) {

            Client.this.downService();
        }
    }

    private void pressNickname() throws IOException {
        System.out.print("Enter your nickName: ");
        nickName = inputUser.readLine();
        out.write(nickName + "\n");
        out.flush();
    }

    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ex) {
        }
    }

    private class ReadMsg extends Thread {
        @Override
        public void run() {

            String str;
            try {
                while ((str = in.readLine()) != null) {
                    if (str.equals("stop")) {
                        Client.this.downService();
                        break;
                    }
                    System.out.println(str);
                }
            } catch (IOException e) {
                Client.this.downService();
            }
        }
    }

    public class WriteMsg extends Thread {

        @Override
        public void run() {
            while (true) {
                String userWord;
                try {
                    userWord = inputUser.readLine();
                    if (userWord.equals("stop")) {
                        out.write("stop" + "\n");
                        Client.this.downService();
                        break;
                    } else {
                        out.write(nickName + ": " + userWord + "\n");
                    }
                    out.flush();
                } catch (IOException e) {
                    Client.this.downService();

                }
            }
        }
    }
}
