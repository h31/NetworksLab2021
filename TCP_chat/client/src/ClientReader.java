import java.io.InputStream;


class ClientReader extends Thread {
  private InputStream is;
  private String username;

  public ClientReader (InputStream is, String username) {
    this.is = is;
    this.username = username;
  }

  @Override
  public void run() {
    String msg = new String();
    MyProtocolClient decoder = new MyProtocolClient(this.is);
    while (true) {
      msg = decoder.decodeMessage(username);
      System.out.println(msg);
    } 
  }
}
