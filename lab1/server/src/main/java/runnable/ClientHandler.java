package runnable;

import model.ExchangeFormat;
import util.Tool;

import java.io.*;
import java.net.Socket;
import java.util.Map;

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
            dOut = new DataOutputStream(clientSocket.getOutputStream());
            dIn = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Soedineniye ustanovleno");


        try {
            greetingNewUser();
            ExchangeFormat clientRequest;
            while (true) {
                System.out.println(ServerStart.clientMap.entrySet() + " <---- текущие активные клиенты");
                String message = in.readLine();

                System.out.println(message);
                clientRequest = Tool.parseRequest(message);

                if (clientRequest.getParcelType().getStringValue().equals(Tool.RequestType.EXIT.getStringValue())) {
                    closeConnections();
                    ServerStart.clientMap.remove(nicknameOfClient);
                    notifyAboutUserExit(nicknameOfClient);
                    break;
                }

                processDefaultMessage(clientRequest);
            }

        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            ServerStart.clientMap.remove(nicknameOfClient);
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
            serverResponse.setAttachmentType(clientRequest.getAttachmentType());
            serverResponse.setAttachmentSize(clientRequest.getAttachmentSize());

            byte[] bytes = new byte[clientRequest.getAttachmentSize()];
            dIn.readFully(bytes);
            System.out.println("Получил");
            serverResponse.setAttachmentByteArray(bytes);
               /* dIn.close();
                dOut.close();*/
        }


        try {
            ServerStart.serverResponseQueue.put(serverResponse);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
