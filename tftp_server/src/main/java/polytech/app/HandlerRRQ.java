import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.File;
import java.nio.file.Files;
import java.net.SocketException;
import java.io.IOException;
import java.net.SocketTimeoutException;

class HandlerRRQ extends Thread {
  private InetAddress address;
  private int port;
  private String filename;
  private String mode;
  private DatagramSocket socket;
  private String dirName = "/tftpboot";

  public HandlerRRQ(InetAddress address, int port, 
    String filename, String modem, DatagramSocket socket) 
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
      HandlerError.sendError(socket, (byte)0, "Server error");
      removeHandler();
    }
    send();
    return;
  }
  
  private void send() {
    File directory = new File(dirName);
    if (!directory.exists()) {
        directory.mkdir();
    }
    File file = new File(dirName + "/" + filename);
    if (file.exists()) {
      try {
        sendFile(file);
      }
      catch (SocketTimeoutException e) {
        HandlerError.sendError(socket, (byte)0, "Server error");
        removeHandler();
      }
    }
    else {
      HandlerError.sendError(socket, (byte)1, "File not found");
      removeHandler(); 
    }
  }

  private void sendFile(File file) throws SocketTimeoutException {
    try {
      byte[] fileBytes = Files.readAllBytes(file.toPath());
      
      int len = fileBytes.length; 
      int send = 0;
      int block = 1;
      while (true) {
        int toSend = len - send;
        System.out.println("block = " + block);
        if (toSend > 512)
          toSend = 512;
        System.out.println("Sending " + toSend + " bytes");
        byte[] buf = new byte[4 + toSend];
        buf[0] = 0;
        buf[1] = 3; //Data
        buf[2] = (byte)(block>>8);
        buf[3] = (byte)block;
        merge(buf, fileBytes, send, toSend);
        int counter = 0;
        while (true) { 
          //send datagram
          DatagramPacket packet = new DatagramPacket(buf, buf.length, 
            address, port);
          socket.send(packet);
          //waiting for ACK
          byte[] ack = new byte[4];
          DatagramPacket getack = new DatagramPacket(ack, ack.length);
          try {
            socket.receive(getack);
            byte[] rec = getack.getData();
            int block_h = (rec[2] & 0xff);
            int block_l = (rec[3] & 0xff);
            int block_rec = (block_h << 8) + (block_l);
            if ((rec[1] == 4) && (block_rec == block)) {
              if (toSend < 512)
                removeHandler();
              else {
                send += toSend;
                block++;
                counter = 0;
                break; 
              } 
            }
            else {
              if (counter == 10) {
                HandlerError.sendError(socket, (byte)0, "Connection lost"); 
                removeHandler();
              }
              counter++;
              continue;
            } 
          }
          catch (SocketTimeoutException e) {
            if (counter == 10) {
              HandlerError.sendError(socket, (byte)0, "Connection lost");
              removeHandler();
            }
            counter++;
            continue;
          }
        }
      }
    }
    catch (IOException e) {
      HandlerError.sendError(socket, (byte)2, "Accsess violation");
      removeHandler();
    }
  }

  //add bytes to buf from data[off] to data[off+len]
  private void merge (byte[] buf, byte[] data, int off, int len) {
     int i = off;
     int j = 4;
     while (i != off + len) {
       buf[j] = data[i];
       i++;
       j++;
     } 
  }
  

  private void removeHandler() {
    
    socket.close();
    this.stop();
  }
  
}
