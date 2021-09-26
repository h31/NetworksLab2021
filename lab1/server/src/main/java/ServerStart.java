import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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


        private void greetingUser() throws IOException {
            out.println("Доброе пожаловать в чат! Введите пожалуйста свой никнейм: "); // personal
            nicknameOfClient = in.readLine();
            /*JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", nicknameOfClient);
            out.println(jsonObject.toString());*/
            for (ClientHandler activeUser : userList) {
                activeUser.out.println("Залогигнился в чат: " + nicknameOfClient);
            }

        }

        @Override
        public void run() {
            System.out.println("Soedineniye ustanovleno");

            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                greetingUser();

                while(true) {
                    String message = in.readLine();
                    System.out.println("Current thread: " + Thread.currentThread().getName() +
                            " Current client nickname: " + message);
                    /*JSONObject jsonObject = new JSONObject();
                    jsonObject.put("msg", message);
                    out.println(jsonObject.toString());*/

                    for (ClientHandler activeUser : userList) {
                        activeUser.out.println(nicknameOfClient + " сказал: вот это: " + message);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
