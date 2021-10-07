package runnable;

import model.ExchangeFormat;
import util.Tool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;

public class ServerStart {

    private ServerSocket serverSocket;

    public static ConcurrentHashMap<String, ClientHandler> clientMap = new ConcurrentHashMap<>();


    public BlockingQueue<ExchangeFormat> serverResponseQueue
            = new ArrayBlockingQueue<>(1000);

    public ExecutorService connectionThreadPool = Executors.newFixedThreadPool(10);

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started");
        new Thread(new QueueHandler(serverResponseQueue)).start();
        while (true) {
            ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
            clientMap.put("", clientHandler);
            connectionThreadPool.execute(clientHandler);
        }
    }

    public class ClientHandler implements Runnable {
        private Socket clientSocket;
        public PrintWriter out;
        public BufferedReader in;
        private String nicknameOfClient;
        public DataInputStream dIn;
        public DataOutputStream dOut;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
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
                while (true) {
                    String message = in.readLine();

                    System.out.println(message);
                    clientRequest = Tool.parseRequest(message);

                    if (clientRequest.getParcelType().getStringValue().equals(Tool.RequestType.EXIT.getStringValue())) {
                        closeConnections();
                        clientMap.remove(nicknameOfClient);
                        notifyAboutUserExit(nicknameOfClient);
                        break;
                    }

                    processDefaultMessage(clientRequest);
                }

            } catch (IOException | ClassNotFoundException | NullPointerException e) {
                e.printStackTrace();
                clientMap.remove(nicknameOfClient);
                Thread.currentThread().interrupt();
            }
        }

        private void greetingNewUser() throws IOException, ClassNotFoundException {
            ExchangeFormat response = new ExchangeFormat();
            ExchangeFormat responseException = new ExchangeFormat();
            ExchangeFormat clientRequest = Tool.parseRequest(in.readLine());

            String usernameDemo = clientRequest.getUsername();

            //validate new user nickname
            while (!validateUsername(usernameDemo)) {
                responseException.setParcelType(Tool.RequestType.EXCEPTION);
                responseException.setMessage("1");
                responseException.setTime(Tool.getCurrentTime());
                out.println(responseException.toParcel());
                clientRequest = Tool.parseRequest(in.readLine());
                usernameDemo = clientRequest.getUsername();
            }

            setVerifiedUsername(usernameDemo);

            //broadcast about new user
            response.setParcelType(Tool.RequestType.GREETING);
            response.setUsername(nicknameOfClient);
            response.setTime(Tool.getCurrentTime());
            try {
                serverResponseQueue.put(response);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private boolean validateUsername(String username) {
            for (Map.Entry<String, ClientHandler> activeUser : ServerStart.clientMap.entrySet()) {
                if (activeUser.getKey().equals(username)) {
                    return false;
                }
            }
            return true;
        }

        private void setVerifiedUsername(String username) {
            for (Map.Entry<String, ClientHandler> entry : clientMap.entrySet()) {
                if (entry.getValue().equals(this)) {
                    ClientHandler handlerVar = clientMap.remove(entry.getKey());
                    clientMap.put(username, handlerVar);
                }
            }
            nicknameOfClient = username;
        }

        private void notifyAboutUserExit(String nicknameOfClient) {
            ExchangeFormat notifyParcel = new ExchangeFormat();
            notifyParcel.setParcelType(Tool.RequestType.EXIT);
            notifyParcel.setUsername(nicknameOfClient);
            notifyParcel.setTime(Tool.getCurrentTime());

            try {
                serverResponseQueue.put(notifyParcel);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void processDefaultMessage(ExchangeFormat clientRequest) throws IOException {
            ExchangeFormat serverResponse = new ExchangeFormat();

            serverResponse.setParcelType(Tool.RequestType.MESSAGE);
            serverResponse.setTime(Tool.getCurrentTime());
            serverResponse.setUsername(nicknameOfClient);
            serverResponse.setMessage(clientRequest.getMessage());

            if (clientRequest.getAttachmentSize() != 0) {
                dOut = new DataOutputStream(clientSocket.getOutputStream());
                dIn = new DataInputStream(clientSocket.getInputStream());
                System.out.println("Клиент вложил файл");
                serverResponse.initializeAttachmentByteArray(clientRequest.getAttachmentSize());
                serverResponse.setAttachmentName(clientRequest.getAttachmentName());
                serverResponse.setAttachmentType(clientRequest.getAttachmentType());
                serverResponse.setAttachmentSize(clientRequest.getAttachmentSize());

                int count;
                byte[] bytes = new byte[clientRequest.getAttachmentSize()];
                dIn.readFully(bytes, 0, bytes.length);
                System.out.println("Получил");
                serverResponse.setAttachmentByteArray(bytes);
               /* dIn.close();
                dOut.close();*/
            }


            try {
                serverResponseQueue.put(serverResponse);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        private void closeConnections() {
            try {
                clientSocket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}