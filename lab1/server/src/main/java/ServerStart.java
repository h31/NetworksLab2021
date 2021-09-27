import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

public class ServerStart {

    private ServerSocket serverSocket;

    public List<ClientHandler> userList = new ArrayList<>();

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started");
        while (true) {
            ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
            userList.add(clientHandler);
            clientHandler.start();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String nicknameOfClient;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        private void greetingNewUser() throws IOException, ClassNotFoundException {


            ServerRequest request = new ServerRequest("greeting",
                    "Добро пожаловать в чат! Введите пожалуйста свой никнейм: ",
                    new Date().toString());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", request.getMessage());
            jsonObject.put("time", request.getTime());
            out.println(request.toString());
            System.out.println("object passed");


            /*while (true) {
                System.out.println("получили: " + in.readLine());
            }*/

        }


        private void greetingUser() throws IOException {

            nicknameOfClient = in.readLine();
            /*JSONObject jsonObject = new JSONObjeыct();
            jsonObject.put("msg", nicknameOfClient);
            out.println(jsonObject.toString());*/
            for (ClientHandler activeUser : userList) {
                activeUser.out.println("Залогигнился в чат: " + nicknameOfClient);
            }

        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Soedineniye ustanovleno");

            try {
                greetingNewUser();
                /*out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                //greetingUser();
                while(true) {
                    String message = in.readLine();
                    System.out.println("Current thread: " + Thread.currentThread().getName() +
                            " Current client nickname: " + message);
                    *//*JSONObject jsonObject = new JSONObject();
                    jsonObject.put("msg", message);
                    out.println(jsonObject.toString());*//*
                    for (ClientHandler activeUser : userList) {
                        activeUser.out.println(nicknameOfClient + " сказал вот это: " + message);
                    }
                }*/

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }


}