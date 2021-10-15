package runnable;

import model.ExchangeFormat;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.*;

public class ServerStart {

    private ServerSocket serverSocket;

    public static ConcurrentHashMap<String, ClientHandler> clientMap = new ConcurrentHashMap<>();


    public static BlockingQueue<ExchangeFormat> serverResponseQueue
            = new ArrayBlockingQueue<>(1000);

    public ExecutorService connectionThreadPool = Executors.newFixedThreadPool(10);

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started");
        QueueHandler queueHandler = new QueueHandler(serverResponseQueue);
        connectionThreadPool.execute(queueHandler);
        while (true) {
            ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
            clientMap.put("", clientHandler);
            connectionThreadPool.execute(clientHandler);
        }


    }




}