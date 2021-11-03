import java.net.Socket;
import java.io.InputStream;
import java.util.Date;
import java.io.IOException; 



//Class is coding message and sending it to other users
class MyProtocolServer {
  private final static int ARRAY_LEN = 150;
  private final static int TEXT_SIZE = 1024;
  private final static int USERNAME_SIZE = 32;
  private final static int FILENAME_SIZE = 128;
  private final static int FILE_SIZE = 500000;

  private InputStream is; 

  public MyProtocolServer(InputStream is) {
    this.is = is;
  }

  public byte[] codeMessage(String msg, String username) {
    MyByteArray arr = new MyByteArray(ARRAY_LEN);
    byte type = this.getType(msg);
    arr.add(type);
    arr.addTime();
    if (username.length() > USERNAME_SIZE)
      return null;
    arr.addString(username); 
    String[] spl = msg.split("/");
    int len = 0;
    switch (type) {
      case 0:
        if (spl[0].length() > TEXT_SIZE)
          return null; 
        arr.addString(spl[0]); //Text
        if (spl[spl.length - 1].length() > FILENAME_SIZE)
          return null;
        arr.addString(spl[spl.length - 1]); //Filename
        len = getInt(); //Filelenght
        if (len >= FILE_SIZE || len == -1) 
          return null;
        if (arr.receiveFile(len, this.is) == -1) //File
          return null; 
        break;
      case 1:
        if (spl[0].length() > TEXT_SIZE)
          return null; 
        arr.addString(spl[0]); //Text
        break;
      case 2: 
        if (spl[spl.length - 1].length() > FILENAME_SIZE)
          return null;
        arr.addString(spl[spl.length - 1]); //Filename
        len = getInt();
        if (len > FILE_SIZE || len == -1)  
          return null;
        if (arr.receiveFile(len, this.is) == -1)
          return null;
        break;
      default: 
        return null;
    }
    return arr.getArray();
  }

  // getType return type (0 - message + file, 1 - message, 2 - file)
  private byte getType(String msg) {
    if (msg.charAt(0) == '/')
      return 2; 
    else if (msg.contains("/"))
      return 0;
    else
      return 1;
  }

  //Get int from InputStream
  private int getInt() {
    int x;
    int ans = 0;
    try {
      for (int i = 24; i >= 0; i -= 8) { 
        x = this.is.read();
        if (x == -1)
          return -1;
        ans += (x << i);
      }
    }
    catch (IOException e) {
      return -1;
    }
    return ans;
  }
}

