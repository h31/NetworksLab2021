import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

class User extends Thread {

  private String username; //Username
  private Socket clientSocket; //Socket
  private InputStream is; 
  private OutputStream os;

  public User(Socket clientSocket, String username,
                  InputStream is, OutputStream os) {
    this.clientSocket = clientSocket;
    this.username = username;
    this.is = is;
    this.os = os;
  }

  public String getUsername() {return this.username;}
  public void setUsername(String username) {this.username = username;}

  @Override
  public void run() {
    System.out.println("user " + this.username + " is listening");

    try {
      MyProtocolServer coder = 
        new MyProtocolServer(this.is);
      byte[] toSend;
      while(true) {
	MyByteArray arr = new MyByteArray(100);
	byte b;
	while (true) {
          b = (byte)is.read();
	  if (b == -1)
            this.removeUser();
	  if (b == 10) // new line
	    break;
	  arr.add(b);
	}
	String msg = new String(arr.getArray());
        System.out.println("new mssadge from user " + this.username);
	System.out.println("msg = " + msg);
	toSend = coder.codeMessage(msg, this.username);
	if (toSend == null) 
	  this.removeUser();
        sendAll(toSend);
      }
    } catch (IOException e) {
      //connection lost, need to remove user from list and stop thread
      this.removeUser();
    }
  }


  private void sendAll(byte[] msg) {
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
      this.os.write(msg);
      this.os.flush();
    } catch (IOException e) {return;}
  }

  private void removeUser() {
    LinkedList<User> userList = ServerTCP.getUserList();
    //other threads can't use userList when we are removing user
    synchronized (userList) {
      //remove user from the list, close streams and socket
      try {
	System.out.println("removing user " + this.username);
        this.is.close();
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
