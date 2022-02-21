package org.example.client;

import org.example.util.NtpPacket;

import java.io.IOException;
import java.net.*;

import static org.example.client.util.ClientUtil.*;

public class Client implements Runnable {

    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] ntpPacket = pack(System.currentTimeMillis());
            DatagramPacket packet;
            packet = new DatagramPacket(ntpPacket, ntpPacket.length, InetAddress.getByName(host), port);
            socket.send(packet);
            DatagramPacket response = new DatagramPacket(ntpPacket, ntpPacket.length);
            socket.setSoTimeout(3 * 1000);
            socket.receive(response);
            byte[] responseArray = response.getData();
            NtpPacket responseNtp = unpack(responseArray);
            System.out.println(responseNtp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
