import java.io.InputStream;


class ClientReader extends Thread {
  private InputStream is;
  private String myUsername;

  public ClientReader (InputStream is, String myUsername) {
    this.is = is;
    this.myUsername = myUsername;
  }

  @Override
  public void run() {
    String msg = new String();
    MyProtocolClient decoder = new MyProtocolClient(this.is, myUsername);
    while (true) {
      decoder.decodeMessage();
    } 
  }
}
