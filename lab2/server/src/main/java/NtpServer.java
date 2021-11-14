import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class NtpServer implements Runnable {

    private DatagramSocket socket;
    private int port = 123;
    private byte[] buf = new byte[48];

    public void run() {
        try {
            System.out.println("Server started");
            socket = new DatagramSocket(port);


            DatagramPacket clientRequestPacket = new DatagramPacket(buf, buf.length);

            socket.receive(clientRequestPacket);
            long timeOfReceiptFromClient = System.currentTimeMillis();

            NtpPacket clientRequest = Util.unpack(buf);

            InetAddress clientAddress = clientRequestPacket.getAddress();
            int clientPort = clientRequestPacket.getPort();
            System.out.println("айпишник клиента = " + clientAddress);

            byte[] byteArray = Util.pack(clientRequest, timeOfReceiptFromClient).toByteArray();
            System.out.println(Arrays.toString(byteArray));
            DatagramPacket packet
                    = new DatagramPacket(byteArray, byteArray.length, clientAddress, clientPort);
            socket.send(packet);
            /*System.out.println(Arrays.toString(buf));

            for (byte b1 : buf){
                String s1 = String.format(
                        "%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');

                System.out.println(s1);
            }
*/
            DatagramPacket serverResponsePacket
                    = new DatagramPacket(buf, buf.length, clientAddress, clientPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
