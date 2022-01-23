import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ChatServer {
    public static void main(String[] args) throws Exception {
        DatagramSocket ss = new DatagramSocket(8777);
        System.out.println("Server started");
        SessionManager manager = new SessionManager();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().interrupt();
            try {
                for (Session s : manager.getSessionList()) {
                    s.shutdown();
                }
                ss.close();
            } finally {
                System.out.println("Server stopped");
            }
        }));


        while (true) {
            byte[] receivingDataBuffer = new byte[65536];
            DatagramPacket inputPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
            ss.receive(inputPacket);
            try {
                manager.getSessionByPacket(inputPacket).handleMessage(inputPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
