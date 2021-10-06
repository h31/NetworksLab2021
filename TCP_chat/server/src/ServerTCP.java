import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ServerTCP { 
  private static final int PORT = 7777; //Server port
  private static LinkedList<User> userList = new LinkedList<User>();

  public static void main (String args[]) {
    ServerTCP.serverWork();
  }

  private static void serverWork() {
    try {
      ServerSocket serverSocket = new ServerSocket(ServerTCP.PORT);
      while(true) {
        //waiting for the connection, return its socket
        Socket clientSocket = serverSocket.accept();
	System.out.println("new client connected");
        //creating buffered reader and buffered writer
        BufferedReader br = new BufferedReader
          (new InputStreamReader(clientSocket.getInputStream()));
	//reading username
	String username = br.readLine();
        //creating a new User
        User newUser = new User(clientSocket, username, br, clientSocket.getOutputStream()); 
        //adding to the list
        userList.add(newUser);
        newUser.start(); //creating a new thread
      }
    } catch (IOException e) {
      System.out.println("Server error, server closed");
      e.getStackTrace();
      System.exit(1);
    }	
  }

  public static LinkedList<User> getUserList() {return ServerTCP.userList;}
}
