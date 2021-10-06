import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

class User extends Thread {

  private String username; //Username
  private Socket clientSocket; //Socket
  private BufferedReader br; //input reader
  private OutputStream os;

  public User(Socket clientSocket, String username,
                  BufferedReader br, OutputStream os) {
    this.clientSocket = clientSocket;
    this.username = username;
    this.br = br;
    this.os = os;
  }

  public String getUsername() {return this.username;}
  public void setUsername(String username) {this.username = username;}

  @Override
  public void run() {
    System.out.println("user " + this.username + " is listening");

    try {
      MyProtocolServer coder = 
        new MyProtocolServer(this.clientSocket.getInputStream());
      byte[] toSend;
      while(true) {
        String msg = this.br.readLine();
        if (msg == null)
          this.removeUser();
        System.out.println("new mssadge from user " + this.username);
	System.out.println("msg = " + msg);
	toSend = coder.codeMessage(msg, this.username);
        sendAll(toSend);
      }
    } catch (IOException e) {
      //connection lost, need to remove user from list and stop thread
      this.removeUser();
    }
  }


  private void sendAll(byte[] msg) {
    System.out.println("msg length = " + msg.length);
    LinkedList<User> userList = ServerTCP.getUserList();
    //other threads can't use userList when we are sending messadge
    synchronized (userList) {
      for (User ur : userList) {
        if (ur != this)
          ur.send(msg);
      }
    }
  }

  protected void send(byte[] msg) {
    try {
      int out = 0;
      this.os.write(msg);
    } catch (IOException e) {return;}
  }

  private void removeUser() {
    LinkedList<User> userList = ServerTCP.getUserList();
    //other threads can't use userList when we are removing user
    synchronized (userList) {
      //remove user from the list, close streams and socket
      try {
	System.out.println("removing user " + this.username);
        this.br.close();
        this.os.close();
        this.clientSocket.close();
        userList.remove(this);
        System.out.println("user removed");
        //JVM will delete object (no references left)
        this.stop();
      } catch(IOException e) {
        e.getStackTrace();
      }
    }
  }
}
