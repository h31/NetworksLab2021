package runnable;

import model.ExchangeFormat;
import util.Tool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerStart {

    private ServerSocket serverSocket;

    public static List<ClientHandler> clientList = new ArrayList<>();

    public BlockingQueue<ExchangeFormat> serverResponseQueue
            = new ArrayBlockingQueue<>(1000);

    public ExecutorService connectionThreadPool = Executors.newFixedThreadPool(10);

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started");
        new Thread(new QueueHandler(serverResponseQueue)).start();
        while (true) {
            ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
            clientList.add(clientHandler);
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
                System.out.println(Thread.currentThread().getName());
                System.out.println(clientList);
                greetingNewUser();

                ExchangeFormat clientRequest;
                while (true) {
                    String message = in.readLine();

                    System.out.println(message);
                    clientRequest = Tool.parseRequest(message);

                    if (clientRequest.getParcelType().getStringValue().equals(Tool.RequestType.EXIT.getStringValue())) {
                        notifyAboutUserExit(nicknameOfClient);
                        closeConnections(); // подумать над очередностью, надо ли присылать клиенту нотифай об его уходе?
                        break;
                    }

                    processDefaultMessage(clientRequest);
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                clientList.remove(this);
                Thread.currentThread().interrupt();
            }
        }

        private void greetingNewUser() throws IOException, ClassNotFoundException {
            ExchangeFormat responseAboutNewUser = new ExchangeFormat();
            ExchangeFormat clientRequest = Tool.parseRequest(in.readLine());

            nicknameOfClient = clientRequest.getUsername();

            //validate new user nickname
            //todo


            //broadcast about new user
            responseAboutNewUser.setParcelType(Tool.RequestType.GREETING);
            responseAboutNewUser.setUsername(nicknameOfClient);
            responseAboutNewUser.setTime(new Date().toString());
            try {
                serverResponseQueue.put(responseAboutNewUser);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void notifyAboutUserExit(String nicknameOfClient) {
            ExchangeFormat notifyParcel = new ExchangeFormat();
            notifyParcel.setParcelType(Tool.RequestType.EXIT);
            notifyParcel.setUsername(nicknameOfClient);
            notifyParcel.setTime(new Date().toString());

            try {
                serverResponseQueue.put(notifyParcel);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void processDefaultMessage(ExchangeFormat clientRequest) throws IOException {
            ExchangeFormat serverResponse = new ExchangeFormat();

            serverResponse.setParcelType(Tool.RequestType.MESSAGE);
            serverResponse.setTime(new Date().toString());
            serverResponse.setUsername(nicknameOfClient);
            serverResponse.setMessage(clientRequest.getMessage());

            //dIn = new DataInputStream(clientSocket.getInputStream());
            /*System.out.println("вот тут насрали");
            dIn.read(serverResponse.getAttachmentByteArray(), 0, clientRequest.getAttachmentSize()); //
            System.out.println("МЫ ЭТО ПРОЧЛИ УДАЧНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + serverResponse.getAttachmentByteArray());
            dOut = new DataOutputStream(clientSocket.getOutputStream());*/


            if(clientRequest.getAttachmentSize() != 0) {
                dOut = new DataOutputStream(clientSocket.getOutputStream());
                dIn = new DataInputStream(clientSocket.getInputStream());
                System.out.println("ОКАЗЫВАЕТСЯ КЛИЕНТ ВЛОЖИЛ ФАЙЛ");
                serverResponse.initializeAttachmentByteArray(clientRequest.getAttachmentSize());
                serverResponse.setAttachmentName(clientRequest.getAttachmentName());
                serverResponse.setAttachmentType(clientRequest.getAttachmentType());
                serverResponse.setAttachmentSize(clientRequest.getAttachmentSize());

                byte[] bytes = new byte[clientRequest.getAttachmentSize()];
                //System.out.println(bytes.length + " размер массива байтов");

                for(int i=0;i<bytes.length;i++){
                    bytes[i]= dIn.readByte();
                }
                //System.out.println(Arrays.toString(bytes));
                serverResponse.setAttachmentByteArray(bytes);
                //System.out.println(serverResponse.getAttachmentByteArray().toString());
            }

            try {
                serverResponseQueue.put(serverResponse);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ///close стрим, два открытых стрима
                /*dIn.close();
                dOut.flush();
                dOut.close();*/
        }


        private void closeConnections() {
            try {
                clientSocket.close();
                in.close();
                out.close();
                dIn.close();
                dOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}