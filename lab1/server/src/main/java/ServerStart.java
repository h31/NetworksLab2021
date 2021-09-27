import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
            ServerRequest response = new ServerRequest("greeting",
                    "Добро пожаловать в чат! Введите пожалуйста свой никнейм: ",
                    new Date().toString());
            out.println(response);
            //System.out.println("Greeting object passed " + response);

            ServerRequest responseAboutNewUser = new ServerRequest();

            ServerRequest clientRequest;
            while (true) {
                clientRequest = parseRequest(in.readLine());
                nicknameOfClient = clientRequest.getMessage();
                responseAboutNewUser.setRequestType("message");
                responseAboutNewUser.setMessage(nicknameOfClient + " <---- вот этот чувак залогинился в чат");
                for (ClientHandler activeUser : userList) {
                    activeUser.out.println(responseAboutNewUser);
                }
            }


            /*while (true) {
                System.out.println("получили: " + in.readLine());
            }*/

        }

        public ServerRequest parseRequest(String format) {
            System.out.println(format);
            String[] array = Arrays.asList(format.split("[({')|(':')|(', ')|('})]"))
                    .stream().filter(str -> !str.isEmpty()).collect(Collectors.toList()).toArray(new String[0]);

            ServerRequest serverRequest = new ServerRequest();
            serverRequest.setTime(new Date().toString());
            serverRequest.setMessage(array[3]);
            serverRequest.setRequestType(array[1]);

            return serverRequest;
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