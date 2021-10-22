package runnable;

import model.ExchangeFormat;
import util.Tool;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    public PrintWriter out;
    public InputStream in;
    private String nicknameOfClient;
    public DataInputStream dIn;
    public DataOutputStream dOut;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {

            in = clientSocket.getInputStream();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            dIn = new DataInputStream(clientSocket.getInputStream());
            dOut = new DataOutputStream(clientSocket.getOutputStream());
            // расшаренный буфер
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Soedineniye ustanovleno");


        try {
            greetingNewUser();
            ExchangeFormat clientRequest;
            while (true) {
                System.out.println(ServerStart.clientMap.entrySet() + " <---- current active client list");
                String message = Tool.readLine(in);

                if (Tool.isClientMessageNull(message)) {
                    killCurrentClient(nicknameOfClient, this);
                    break;
                }
                clientRequest = Tool.parseRequest(message);

                System.out.println("получена посылка от клиента: " + message);

                if (clientRequest.getParcelType().getStringValue().equals(Tool.RequestType.EXIT.getStringValue())) {
                    closeConnections();
                    ServerStart.clientMap.remove(nicknameOfClient);
                    notifyAboutUserExit(nicknameOfClient);
                    break;
                }


                processDefaultMessage(clientRequest);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            ServerStart.clientMap.remove(nicknameOfClient);
            Thread.currentThread().interrupt();
        }
    }

    private void greetingNewUser() throws IOException, ClassNotFoundException {
        ExchangeFormat response = new ExchangeFormat();
        ExchangeFormat responseException = new ExchangeFormat();
        String clientGreetingMessage = Tool.readLine(in);

        if (Tool.isClientMessageNull(clientGreetingMessage)) {
            killCurrentClient("", this);
            return;
        }

        ExchangeFormat clientRequest = Tool.parseRequest(clientGreetingMessage);

        String usernameDemo = clientRequest.getUsername();

        //validate new user nickname
        while (!validateUsername(usernameDemo)) {
            responseException.setParcelType(Tool.RequestType.EXCEPTION);
            responseException.setMessage("1");
            responseException.setTime(Tool.getCurrentTime());
            out.println(responseException.toParcel());
            clientGreetingMessage = Tool.readLine(in);
            if (Tool.isClientMessageNull(clientGreetingMessage)) {
                killCurrentClient(nicknameOfClient, this);
                return;
            }
            clientRequest = Tool.parseRequest(clientGreetingMessage);
            usernameDemo = clientRequest.getUsername();
        }

        setVerifiedUsername(usernameDemo);

        //broadcast about new user
        response.setParcelType(Tool.RequestType.GREETING);
        response.setUsername(nicknameOfClient);
        response.setTime(Tool.getCurrentTime());
        try {
            ServerStart.serverResponseQueue.put(response);
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
        for (Map.Entry<String, ClientHandler> entry : ServerStart.clientMap.entrySet()) {
            if (entry.getValue().equals(this)) {
                ClientHandler handlerVar = ServerStart.clientMap.remove(entry.getKey());
                ServerStart.clientMap.put(username, handlerVar);
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
            ServerStart.serverResponseQueue.put(notifyParcel);
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
            System.out.println("Клиент вложил файл");
            serverResponse.initializeAttachmentByteArray(clientRequest.getAttachmentSize());
            serverResponse.setAttachmentName(clientRequest.getAttachmentName());
            serverResponse.setAttachmentSize(clientRequest.getAttachmentSize());

            byte[] bytes = new byte[clientRequest.getAttachmentSize()];

            System.out.println("File received from client ");
            dIn.readFully(bytes);
            serverResponse.setAttachmentByteArray(bytes);
        }


        try {
            ServerStart.serverResponseQueue.put(serverResponse);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void killCurrentClient(String nickname, ClientHandler clientHandler) {
        ServerStart.clientMap.remove(nickname, clientHandler);
        closeConnections();
        Thread.currentThread().interrupt();
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
