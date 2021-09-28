import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            ExchangeFormat responseAboutNewUser = new ExchangeFormat();
            ExchangeFormat clientRequest = Util.parseRequest(in.readLine());

            nicknameOfClient = clientRequest.getMessage();

            //validate new user nickname
            //todo


            //broadcast about new user
            responseAboutNewUser.setParcelType(Util.Request.INFO.getStringValue());
            responseAboutNewUser.setUsername(nicknameOfClient);
            responseAboutNewUser.setTime(new Date().toString());
            for (ClientHandler activeUser : userList) {
                activeUser.out.println(responseAboutNewUser);

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

                ExchangeFormat clientRequest;
                ExchangeFormat messageBroadcastResponse = new ExchangeFormat();
                while (true) {
                    clientRequest = Util.parseRequest(in.readLine());
                    if(clientRequest.getParcelType().equals(Util.Request.EXIT.toString())) {
                        clientSocket.close();
                        notifyAboutUserExit(nicknameOfClient);
                        break;
                    }

                    messageBroadcastResponse.setParcelType(Util.Request.MESSAGE.toString());
                    messageBroadcastResponse.setTime(new Date().toString());
                    messageBroadcastResponse.setUsername(nicknameOfClient);
                    messageBroadcastResponse.setMessage(clientRequest.getMessage());

                    for (ClientHandler activeUser : userList) {
                        activeUser.out.println(messageBroadcastResponse);
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void notifyAboutUserExit(String nicknameOfClient) {
            ExchangeFormat notifyParcel = new ExchangeFormat();
            notifyParcel.setParcelType(Util.Request.EXIT.toString());
            notifyParcel.setUsername(nicknameOfClient);
            notifyParcel.setTime(new Date().toString());

            for (ClientHandler activeUser : userList) {
                activeUser.out.println(notifyParcel);

            }
        }

    }


}