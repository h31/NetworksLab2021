package Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageReader {
    public static byte[] readMessage(SocketChannel socket) throws IOException {
        ByteBuffer bb1 = ByteBuffer.allocate(12);
        socket.read(bb1);
        String result = new String(bb1.array()).trim();
        String len2 = result.substring(6, 12);
        ByteBuffer bb2 = ByteBuffer.allocate(Integer.parseInt(len2));
        socket.read(bb2);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(bb1.array());
        outputStream.write(bb2.array());
        return outputStream.toByteArray();
    }
}
