import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.net.Socket;

public class Client {
  private final static int TEXT_SIZE = 1024;
  private final static int USERNAME_SIZE = 32;
  private final static int FILENAME_SIZE = 128;
  private final static int FILE_SIZE = 500000;

  public static void main(String[] args){
    try { 
      //Enter username
      System.out.println("Enter your username");
      String username = System.console().readLine();
      if (username.length() > USERNAME_SIZE) {
        System.out.println("Username size is too big");
        return;
      }
      //creating a socket connected to the server
      Socket clientSocket = new Socket ("localhost", 7777);
      //creating buffered reader and buffered writer
      BufferedWriter bw = new BufferedWriter (
        new OutputStreamWriter(clientSocket.getOutputStream()));
      bw.write(username + "\n");
      bw.flush();
      ClientReader clRd = new ClientReader (
        clientSocket.getInputStream(), username);
      clRd.start();
      OutputStream os = clientSocket.getOutputStream();
      while (true) {
        sendMessages(os, bw);
      }
    }
    catch (IOException e) {
      System.out.println("Server lost");
      System.exit(-1);
    }
  }

  private static void sendMessages(OutputStream os, BufferedWriter bw) {
    System.out.println("Enter messsage");
    String msg = System.console().readLine();
    if (msg.length() > TEXT_SIZE) {
      System.out.println("Text size is too big");
      return;
    }
    System.out.println("Enter filename");
    String filename = System.console().readLine();
    if (filename.length() > FILENAME_SIZE) {
      System.out.println("Filename size is too big");
    }
    byte type;
    if (msg == null || filename == null) {
      System.out.println("Try again");
      return;
    }
    if (msg.contains("/")) {
      System.out.println("Messge contains reserved char '/', sending denied");
      return;
    }

    if (!msg.isEmpty() && !filename.isEmpty())
      type = 0;
    else if (!msg.isEmpty() && filename.isEmpty())
      type = 1;
    else if (msg.isEmpty() && !filename.isEmpty())
      type = 2;
    else return;
    File f;
    int len;
    try {
      switch (type) {
        case 0:
           f = new File(filename);
           if (!f.exists()) {
             System.out.println("File " + filename + "do not exist");
             return;
           }
           msg = msg + "/" + filename + "\n";
           send(os, f, msg); 
            break;
        case 1:
          msg = msg + "\n";
          bw.write(msg);
          bw.flush();
          break;
        case 2:
          f = new File(filename);
          if (!f.exists()) {
            System.out.println("File " + filename + " do not exist");
            return;
          }
          msg = "/" + filename + "\n";
          send(os, f, msg);
          break;
      }
    return;
    }
    catch (IOException e) {
      System.out.println("Server lost");
      System.exit(-1);
    }
  }

  private static void send(OutputStream os, File f, String msg) {
    try {
      byte[] fileBytes = Files.readAllBytes(f.toPath());
      if (fileBytes.length > FILE_SIZE) {
        System.out.println("File size is too big");
        return;
      }
      os.write(msg.getBytes());
      writeInt(os, fileBytes.length);
      os.write(fileBytes);
      os.flush();
    }
    catch (IOException e) {
      System.out.println("Server lost");
      System.exit(-1);
    }
  }

  private static void writeInt(OutputStream os, int x) {
    try {
      for (int i = 24; i >= 0; i -= 8) {
        os.write((byte)(x >> i));
      }
      return;
    }
    catch (IOException e) {
      System.out.println("Server lost");
      System.exit(-1);
    }
  }
}
