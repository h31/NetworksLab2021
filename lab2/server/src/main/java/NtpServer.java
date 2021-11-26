import java.io.IOException;
import java.net.*;
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
        byte[] hostAddress = new byte[4];

        try {
            hostAddress = InetAddress.getLocalHost().getAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                socket.receive(clientRequestPacket);
            long timeOfReceiptFromClient = System.currentTimeMillis();

            NtpPacket clientRequest = Util.unpack(buf);

            InetAddress clientAddress = clientRequestPacket.getAddress();
            int clientPort = clientRequestPacket.getPort();
            System.out.println("айпишник клиента = " + clientAddress);

            byte[] byteArray = Util.pack(clientRequest, timeOfReceiptFromClient, hostAddress).toByteArray();

            DatagramPacket packet
                    = new DatagramPacket(byteArray, byteArray.length, clientAddress, clientPort);
            socket.send(packet);

            //clear bytearray
            Arrays.fill(buf, (byte)0);

            } catch (IOException e) {
                e.printStackTrace();
                socket.close();
                return;
            }

        }
    }
}
