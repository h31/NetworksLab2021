import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;


class HandlerRRQ {
  private InetAddress address;
  private static final int rrqPort = 69;
  private int serverHandlerPort; 
  private String filename;
  private DatagramSocket socket;
  private FileChannel fc = null;
  private FileLock fl = null;

  public HandlerRRQ(String adr, String filename) {
    try {
      this.address = InetAddress.getByName(adr);
      this.filename = filename;
    }
    catch (UnknownHostException e) {
      System.out.println("Wrong IP");
      System.exit(1);
    }
  }
  
  public void handle() {
    try {
      socket = new DatagramSocket();
      socket.setSoTimeout(3000); // set timeout 
    }
    catch (SocketException e) {
      System.exit(1); 
    }
    getFile();
  }

  public void getFile() {
    try {
      File file = new File(filename);
      int block = 0;
      int counter = 0;
      fc = new FileOutputStream(file).getChannel();
      while (fl == null) {
        fl = fc.tryLock();
      }
      while (true) {
        byte[] msg; 
        if (block == 0) {
          msg = HandlerAssistant.getRequest((byte)1, filename, "octet");
          HandlerAssistant.sendPacket(msg, socket, address, rrqPort);
        }
        else {
          msg = HandlerAssistant.getAck(block);
          HandlerAssistant.sendPacket(msg, socket, address, serverHandlerPort);
        }
        byte[] buf = new byte[516];
        DatagramPacket getBuf = new DatagramPacket(buf, buf.length);
        try {
          socket.receive(getBuf);
          serverHandlerPort = getBuf.getPort();
          int rec = getBuf.getLength();
          byte[] bytes = getBuf.getData();
          int block_h = (bytes[2] & 0xff);
          int block_l = (bytes[3] & 0xff);
          int block_rec = (block_h << 8) + (block_l);
          if ((bytes[1] == 3) && (block_rec == block + 1)) {
            counter = 0;
            block++;
            ByteBuffer bBuf = ByteBuffer.wrap(bytes, 4, rec - 4);
            fc.write(bBuf);
            if (rec < 516) {
              msg = HandlerAssistant.getAck(block);
              HandlerAssistant.sendPacket(msg, socket, 
                address, serverHandlerPort);
              System.out.println("Received blocks: " + block);
              removeHandler();
              return;
            }
            else {
              if (counter == 5) {
                System.out.println("Connection lost");
                removeHandler();
                return;
              }
              counter++;
              continue;
            }
          }
          else if (bytes[1] == 5) { //error opcode
            HandlerAssistant.handleError(bytes, rec);
            removeHandler();
            return;
          }
          else {
            if (counter == 5) {
              System.out.println("Connection lost");
              removeHandler();
              return;
            }
            counter++;
            continue;
          }
        }
        catch (SocketTimeoutException e) {
          if (counter == 5) {
            System.out.println("Connection lost");
            removeHandler();
            return;
          }
          counter++;
          continue;
        }
      }
    }
    catch (IOException e) {
      System.out.println("Access violation");
      removeHandler();
      return;
    }
  }
  
  private void removeHandler() {
    try {
      if (fl != null)
        fl.close();
      if (fc != null)
        fc.close();
      socket.close();
    }
    catch(IOException e) {
      System.out.println("Socket error");
      System.exit(1);
    }
  }
}
