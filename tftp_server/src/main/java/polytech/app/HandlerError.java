import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

class HandlerError {

  public static void sendError(DatagramSocket socket, InetAddress adr, 
    int port, byte code, String msg) { 
    try {
      byte[] strBytes = msg.getBytes("ASCII");
      byte[] errorMsg = new byte [5 + strBytes.length];
      errorMsg[0] = 0;
      errorMsg[1] = 5;
      errorMsg[2] = 0;
      errorMsg[3] = code;
      int i = 0;
      int j = 4;
      while (i != strBytes.length) {
        errorMsg[j] = strBytes[i];
        i++;
        j++; 
      }
      errorMsg[4 + strBytes.length] = 0;
      DatagramPacket packet = new DatagramPacket(errorMsg, errorMsg.length,
        adr, port);
      socket.send(packet);
    }
    catch (UnsupportedEncodingException e) {
      return;
    }
    catch (IOException e) {
      return;
    }
  }
}
