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
  private static final int wrqPort = 69;
  private String filename;
  private DatagramSocket socket;

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
      socket.setSoTimeout(3000); // set timeout 
    }
    catch (SocketException e) {
      System.out.println("Socket error");
      removeHandler();
    }
    send();
    return;
  }
  private void send() {
    File file = new File(filename);
    if (file.exists()) {
      try {
        sendFile(file);
      }
      catch (SocketTimeoutException e) {
        System.out.println("Socket error");
        removeHandler();
      }
    }
    else {
      System.out.println("File " + filename + " doesn't exist");
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
        if (toSend > 512)
          toSend = 512;
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
    }
    catch (IOException e) {
      HandlerError.sendError(socket, address, port,
        (byte)2, "Accsess violation");
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

