package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 1111));

        Thread t = new SendThread(socketChannel, args[0]);
        t.start();
        Thread tt = new ReceiveThread(socketChannel);
        tt.start();
    }
}
