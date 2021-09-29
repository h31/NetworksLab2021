import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerStart {

    private ServerSocket serverSocket;

    public List<ClientHandler> userList = new ArrayList<>();

    public ExecutorService connectionThreadPool = Executors.newFixedThreadPool(10);

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started");
        while (true) {
            ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
            userList.add(clientHandler);
            connectionThreadPool.execute(clientHandler);
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private class ClientHandler implements Runnable {
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

            nicknameOfClient = clientRequest.getUsername();

            //validate new user nickname
            //todo


            //broadcast about new user
            responseAboutNewUser.setParcelType(Util.Request.GREETING.getStringValue());
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
                    String message = in.readLine();
                    ExchangeFormat request = Util.parseRequest(message);
                    byte[] bytes = new byte[Integer.parseInt(request.getAttachmentType())];
                    System.out.println(bytes.length);
                    DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                    inputStream.readFully(bytes, 0, bytes.length);
                    System.out.println(Arrays.toString(bytes));
                    FileOutputStream fos = new FileOutputStream("F:\\Projects\\TKS-lab\\networkslab2021\\lab1\\ejuda.jpg");
                    fos.write(bytes);
                    DataOutputStream dOut = new DataOutputStream(clientSocket.getOutputStream());
                    dOut.write(bytes);



                    clientRequest = Util.parseRequest(message);
                    if(clientRequest.getParcelType().equals(Util.Request.EXIT.toString())) {
                        clientSocket.close();
                        notifyAboutUserExit(nicknameOfClient);
                        break;
                    }

                    messageBroadcastResponse.setParcelType(Util.Request.MESSAGE.getStringValue());
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