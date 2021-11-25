import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.net.SocketException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;


class HandlerWRQ extends Thread {
  private InetAddress address;
  private int port;
  private String filename;
  private String mode;
  private DatagramSocket socket;
  private String dirName = "/tftpboot";
  private FileChannel fc = null;
  private FileLock fl = null; 

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
    try {
      socket.setSoTimeout(5000); // set timeout 
    }
    catch (SocketException e) {
      HandlerError.sendError(socket, address, port, (byte)0, "Server error");
      removeHandler();
    }
    File directory = new File(dirName);
    if (!directory.exists()) {
        directory.mkdir();
    }
    File file = new File(dirName + "/" + filename);
    getFile(file);
  }

  public void getFile(File file) { 
    try {
      int block = 0;
      int counter = 0;
      fc = new FileOutputStream(file).getChannel();
      while (fl == null) {
        fl = fc.tryLock();
      }
      while (true) {
        sendAck(block);
        byte[] buf = new byte[516];
        DatagramPacket getBuf = new DatagramPacket(buf, buf.length);
        try {
          socket.receive(getBuf);
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
              sendAck(block);
              removeHandler();
            } 
            else {
              if (counter == 5) {
                HandlerError.sendError(socket, address, port,
                  (byte)0, "Connection lost");
                removeHandler();
                }
                counter++;
                continue;
              }
            }
          else {
            if (counter == 5) {
              HandlerError.sendError(socket, address, port,
                (byte)0, "Connection lost");
              removeHandler();
            }
            counter++;
            continue;
          }
        }
        catch (SocketTimeoutException e) {
          if (counter == 5) {
            HandlerError.sendError(socket, address, port,
              (byte)0, "Connection lost");
            removeHandler();
          }
          counter++;
          continue;
        }
      }
    } 
    catch (IOException e) {
      HandlerError.sendError(socket, address, port,
        (byte)2, "Accsess violation");
      removeHandler(); 
    } 
  }

  private void sendAck(int block) {
    try { 
      byte[] ack = new byte[4];
      ack[0] = 0;
      ack[1] = 4;
      ack[2] = (byte)(block>>8);
      ack[3] = (byte)(block);
      DatagramPacket packet = new DatagramPacket(ack, ack.length,
        address, port);
      socket.send(packet);
    }
    catch (IOException e) {
      HandlerError.sendError(socket, address, port, (byte)0, "Server error");
      removeHandler();
    }
  } 
  private void removeHandler() {
    try {
      if (fl != null)
        fl.close();
      if (fc != null)
        fc.close();
      socket.close();
      this.stop();
    }
    catch(IOException e) {
      HandlerError.sendError(socket, address, port, (byte)0, "Server error");
      System.exit(1);
    }
  }
}

