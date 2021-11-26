import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;

class HandlerAssistant {

  public static void handleError(byte[] msg) {
  }

  public static byte[] getAck(int block) {
    byte[] ack = new byte[4];
    ack[0] = 0;
    ack[1] = 4;
    ack[2] = (byte)(block>>8);
    ack[3] = (byte)(block);
    return ack;
  }

  

  public static byte[] getRequest(byte opcode, String filename, String mode) {
    byte[] msg = new byte[4 + filename.length() + mode.length()];
    msg[0] = 0;
    msg[1] = opcode;
    int i = 2;
    i = addString(msg, filename, i);
    addString(msg, mode, i);
    return msg;
  }

  private static int addString(byte[] msg, String str, int i) {
    try {
      byte[] bytes = str.getBytes("ASCII");
      for (int j = 0; j != bytes.length; j++) {
        msg[i] = bytes[j];
        i++;
      } 
      msg[i] = 0;
      i++;
      return i; 
    } 
    catch (UnsupportedEncodingException e) {
      System.out.println("Unsupported encoding");
      System.exit(1);
    }
    return 0;
  }

  public static void sendPacket(byte[] msg, DatagramSocket socket, InetAddress address, int port) {
    try {
      DatagramPacket packet = new DatagramPacket(msg, msg.length,
        address, port);
      socket.send(packet);
    }
    catch (IOException e) {
      System.out.println("Socket error");
      System.exit(1);
    }
  }
}
