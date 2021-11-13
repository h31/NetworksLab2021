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
  private static Integer i;
  private static byte[] msg;

  public static void main(String[] args) throws IOException{
    try {
      DatagramSocket serverSocket = new DatagramSocket(PORT);
      DatagramPacket inputPacket = new DatagramPacket(buf, buf.length);
      while (true) {
        serverSocket.receive(inputPacket);
        msg = inputPacket.getData();
        byte type = msg[1];   
        System.out.println("getting filename");
        i = 2;
        String filename = getString();
        String mode = getString();
        if (filename == null)
          continue;
        DatagramSocket handlerSocket = new DatagramSocket();
        switch (type) {
          case(1):
            HandlerRRQ hRRQ = new HandlerRRQ(inputPacket.getAddress(), 
              inputPacket.getPort(), filename, mode, handlerSocket);
            hRRQ.start(); 
            break;
          case(2):
            HandlerWRQ hWRQ = new HandlerWRQ(inputPacket.getAddress(),
              inputPacket.getPort(), filename, mode, handlerSocket);
            hWRQ.start();
            break;
          default:
            HandlerError.sendError(handlerSocket, 
              (byte)4, "Illegal TFTP operation");
            break;
        }
      }
    }
    catch (SocketException e) {
      e.printStackTrace();
    }
  }

  private static String getString() {
    int _i = i;
    while (_i < msg.length) {
      if (msg[_i] == 0)
        break;
      _i++;
    } 
    System.out.println(_i);
    if (_i == msg.length - 1)
      return null;
    else {
      String str = new String();
      try {
        str = new String(Arrays.copyOfRange(msg, i, _i), "ascii");
      }
      catch (UnsupportedEncodingException e) {
        System.exit(1);
      }
      i = _i + 1;
      return str;
    }
  }
}
