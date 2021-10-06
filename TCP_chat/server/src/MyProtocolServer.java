import java.net.Socket;
import java.io.InputStream;
import java.util.Date;
import java.io.IOException; 



//Class is coding message and sending it to other users
class MyProtocolServer {
  private final static int ARRAY_LEN = 150;
  private final static int TEXT_SIZE = 512;
  private final static int USERNAME_SIZE = 64;
  private final static int FILENAME_SIZE = 64;
  private final static int FILE_SIZE = 100000;

  private InputStream is; 

  public MyProtocolServer(InputStream is) {
    this.is = is;
  }

  public byte[] codeMessage(String msg, String username) {
    Integer pos = new Integer(0);
    byte[] msgArr = new byte[ARRAY_LEN];
    byte type = this.getType(msg);
    this.add(msgArr, type, pos);
    this.addTime(msgArr, pos);
    if (username.length() > USERNAME_SIZE)
      return null;
    this.addString(msgArr, username, pos); 
    String[] spl = msg.split("/");
    int len = 0;
    switch (type) {
      case 0:
	if (spl[0].length() > TEXT_SIZE)
	  return null;	
	this.addString(msgArr, spl[0], pos); //Text
	if (spl[spl.length - 1].length() > FILENAME_SIZE)
	  return null;
	this.addString(msgArr, spl[spl.length - 1], pos); //Filename
	len = getInt(); //Filelenght
	if (len >= FILE_SIZE || len == -1) 
	  return null;
	if (this.receiveFile(msgArr, len, pos) == -1) //File
	  return null;	
        break;
      case 1:
        if (spl[0].length() > TEXT_SIZE)
	  return null;	
	this.addString(msgArr, spl[0], pos); //Text
        break;
      case 2: 
	if (spl[spl.length - 1].length() > FILENAME_SIZE)
	  return null;
	this.addString(msgArr, spl[spl.length - 1], pos); //Filename
	len = getInt();
	if (len > FILE_SIZE || len == -1)  
	  return null;
	if (this.receiveFile(msgArr, len, pos) == -1)
 	  return null;
	break;
      default: 
	return null;
    }
    return msgArr;
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

  //Adding time to the messege
  private void addTime(byte[] msgArr, Integer pos) {
    Date d = new Date();
    long time = d.getTime();
    for (int i = 56; i >= 0; i -= 8)
      this.add(msgArr, (byte)(time >> i), pos); 
    return;
  }

  //Adding int to the messege
  private void addInt(byte[] msgArr, int n, Integer pos) {
    for (int i = 24; i >= 0; i -= 8) 
      this.add(msgArr, (byte)(n >> i), pos); 
    return;
  }

  //Get int from InputStream
  private int getInt() {
    int x;
    int ans = 0;
    try {
      for (int i = 24; i >= 0; i -= 8) {
        x = this.is.read();
        ans += (x << i);
      }
    }
    catch (IOException e) {
      return -1;
    }
    return ans;
  }

  //Add str.lenght and str as bytes to the message
  private void addString(byte[] msgArr, String str, Integer pos) {
    byte[] bytes = str.getBytes();
    this.addInt(msgArr, bytes.length, pos);
    this.addAll(msgArr, bytes, pos);
    return;
  }

  private int receiveFile(byte[] msgArr, int len, Integer pos) {
    int rec = 0; //bytes received
    byte[] buf = new byte[1024]; //buffer
    try {
      while (rec != len) {
        rec += this.is.read(buf, rec, len - rec);
        this.addAll(msgArr, buf, pos);
      }
    } catch (IOException e) {return -1;}
    return 0;

  }

  private void add(byte[] msgArr, byte data, Integer pos) {
    if (pos == msgArr.length - 1)
      msgArr = this.expandArray(msgArr, pos);
    msgArr[pos] = data;
    pos++;
    return;
  }

  private void addAll(byte[] msgArr, byte[] data, Integer pos) {
    for (int i = 0; i < data.length; i++)
      this.add(msgArr, data[i], pos);
    return;
  }

  private byte[] expandArray(byte[] msgArr, Integer pos) {
    byte[] newArray = new byte[(int)(msgArr.length * 1.5 + 1)];
    for (int i = 0; i < pos; i++)
      newArray[i] = msgArr[i];
    return newArray;
  }
}

