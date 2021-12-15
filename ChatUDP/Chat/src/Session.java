import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class Session {
    private static int count = 0;
    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;
    private String nickname;
    private final Coder coder = new Coder();
    private final SessionManager manager;

    public Session(InetAddress address, int port, SessionManager manager) throws IOException {
        this.socket = new DatagramSocket();
        this.address = address;
        this.port = port;
        this.manager = manager;
        this.nickname = "guest" + ++count;
        send("type=init&nickname=" + nickname);
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }


    private void send(String str) {
        byte[] buffer = str.getBytes(StandardCharsets.UTF_8);
        DatagramPacket p = new DatagramPacket(buffer, buffer.length, address, port);
        try {
            socket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void close() {
        try {
            socket.close();
        } finally {
            manager.close(this);
        }
    }


    public void shutdown() {
        send("type=shutdown");
        close();
    }


    public void handleMessage(DatagramPacket packet) {
        String msg = new String(packet.getData(), 0, packet.getLength());

        Map<String, String> input = coder.decodeData(msg);

        String type = input.get("type");

        switch (type) {
            case "nickname" -> nickname = input.get("nickname");
            case "text" -> {
                Map<String, String> data = new HashMap<>();
                data.put("type", "text");
                data.put("timestamp", String.valueOf(getUTCTimestamp()));
                data.put("nickname", nickname);
                data.put("text", input.get("text"));
                String output = coder.encodeData(data);
                for (Session s : manager.getSessions()) {
                    if (s != this) {
                        s.send(output);
                    }
                }
            }
        }
    }

    static long getUTCTimestamp() {
        return Instant.now().toEpochMilli() / 1000L;
    }

}