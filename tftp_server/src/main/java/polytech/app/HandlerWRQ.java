import java.net.DatagramSocket;
import java.net.InetAddress;

class HandlerWRQ extends Thread {
  private InetAddress address;
  private int port;
  private String filename;
  private String mode;
  private DatagramSocket socket;

  public HandlerWRQ(InetAddress address, int port,
    String filename, String mode, DatagramSocket socket)
  {
    this.address = address;
    this.port = port;
    this.filename = filename;
    this.mode = mode;
    this.socket = socket;

  }

  @Override
  public void run() {
  }
}

