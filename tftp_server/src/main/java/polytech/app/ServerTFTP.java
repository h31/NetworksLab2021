import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class ServerTFTP {
  private static final int PORT = 69;
  private static byte[] buf = new byte[512];

  public static void main(String[] args) throws IOException{
    try {
      DatagramSocket serverSocket = new DatagramSocket(PORT);
      DatagramPacket inputPacket = new DatagramPacket(buf, buf.length);
      while (true) {
        serverSocket.receive(inputPacket);
        byte[] msg = inputPacket.getData();
        byte type = msg[1];   
        System.out.println("getting filename");
        String filename = getFilename(msg);
        if (filename == null)
          continue;
        System.out.println(filename);
        if (type == 1) 
          System.out.println("RRQ");
        else if (type == 2)
          System.out.println("WRQ");
        else {
          System.out.println("unknown");
          System.out.println(msg);
        }
      }
    }
    catch (SocketException e) {
      e.printStackTrace();
    }
  }

  private static String getFilename(byte[] msg) {
    int i = 2;
    while (i < msg.length) {
      if (msg[i] == 0)
        break;
      i++;
    } 
    System.out.println(i);
    if (i == msg.length - 1)
      return null;
    else {
      String filename = new String();
      try {
        filename = new String(Arrays.copyOfRange(msg, 2, i), "ASCII");
      }
      catch (UnsupportedEncodingException e) {
        System.exit(1);
      }
      return filename;
    }
  }
}
