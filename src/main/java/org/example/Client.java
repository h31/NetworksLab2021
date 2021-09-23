package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String userName;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.print("Enter username: ");
        Scanner scanner = new Scanner(System.in);
        userName = scanner.nextLine();
        String str;
        String attach, message;
        while (!".".equals(str = scanner.nextLine())) {
            String[] strings = str.split(" -a ");
            if (strings.length == 1) {
                str = str.trim();
                if (str.startsWith("-a ")) {
                    System.out.println("attachment");
                } else {
                    System.out.println("just message");
                }
            } else if (strings.length == 2) {
                message = strings[0];
                attach = strings[1];
                File file = new File(attach);
                System.out.println( file.getName());
                Path fileLocation = Paths.get(attach);
                byte[] data = Files.readAllBytes(fileLocation);
                Base64.getEncoder().encodeToString(data);
                System.out.println(Base64.getDecoder().decode(data));
            } else {
                System.out.println("хуй там плавал");
            }
        }
    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
