import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class NtpServer implements Runnable {

    private DatagramSocket socket;
    private final int PORT = 123;
    private byte[] buf = new byte[48];

    public void run() {
        System.out.println("Server started");
        try {
            socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        DatagramPacket clientRequestPacket = new DatagramPacket(buf, buf.length);

        while (true) {
            try {
                socket.receive(clientRequestPacket);
            long timeOfReceiptFromClient = System.currentTimeMillis();

            NtpPacket clientRequest = Util.unpack(buf);

            InetAddress clientAddress = clientRequestPacket.getAddress();
            int clientPort = clientRequestPacket.getPort();
            System.out.println("айпишник клиента = " + clientAddress);

            byte[] byteArray = Util.pack(clientRequest, timeOfReceiptFromClient).toByteArray();

            DatagramPacket packet
                    = new DatagramPacket(byteArray, byteArray.length, clientAddress, clientPort);
            socket.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
