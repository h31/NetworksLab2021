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
  public static void main(String[] args){
    try {
      //Enter username
      System.out.println("Enter your username");
      String username = System.console().readLine();
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
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void sendMessages(OutputStream os, BufferedWriter bw) {
    System.out.println("Enter messsage");
    String msg = System.console().readLine();
    System.out.println("Enter filename");
    String filename = System.console().readLine();
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
	  bw.write(msg);
	  bw.flush();
	  sendFile(os, f);	
          break;
        case 1:
	  msg = msg + "\n";
	  bw.write(msg);
	  bw.flush();
	  break;
        case 2:
	  f = new File(filename);
          if (!f.exists()) {
            System.out.println("File " + filename + "do not exist");
            return;
          }
	  filename = "/" + filename + "\n";
	  os.write(filename.getBytes());
	  sendFile(os, f);
	  break;
      }
      return;
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void sendFile(OutputStream os, File f) {
    try {
      byte[] fileBytes = Files.readAllBytes(f.toPath());
      writeInt(os, fileBytes.length);
      os.write(fileBytes);
    }
    catch (IOException e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }
}
