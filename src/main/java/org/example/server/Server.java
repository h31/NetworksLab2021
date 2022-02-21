package org.example.server;

import org.example.util.NtpPacket;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

import static org.example.server.util.Util.pack;
import static org.example.server.util.Util.unpack;

public class Server implements Runnable {

    private final int PORT = 123;
    private final DatagramSocket socket;
    private final byte[] buf = new byte[48];

    public Server() throws SocketException {
        this.socket = new DatagramSocket(PORT);
    }

    @Override
    public void run() {
        DatagramPacket clientPacket = new DatagramPacket(buf, buf.length);
        byte[] hostAddress = new byte[4];

        try {
            hostAddress = InetAddress.getLocalHost().getAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                socket.receive(clientPacket);
                long receiveTime = System.currentTimeMillis();

                NtpPacket clientRequest = unpack(buf);

                InetAddress clientAddress = clientPacket.getAddress();
                int clientPort = clientPacket.getPort();

                byte[] byteArray = pack(clientRequest, receiveTime, hostAddress).toByteArray();

                DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length, clientAddress, clientPort);
                socket.send(packet);
                Arrays.fill(buf, (byte) 0);
            } catch (IOException e) {
                e.printStackTrace();
                socket.close();
            }
        }
    }
}
