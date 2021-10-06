import java.io.InputStream;
import java.io.IOException;
import java.util.TimeZone;
import java.util.Date;
import java.io.File;
import java.io.FileWriter;

class MyProtocolClient {
  private InputStream is;

  public MyProtocolClient (InputStream is) {
    this.is = is;
  }

  public String decodeMessage(String username) {
    String message = new String();
    try {
    int type = is.read();
    this.addTime(message);
    message += "[" + this.getText() + "] ";
    switch (type) {
      case 0:
	message += this.getText();
	this.receiveFile(username);
        break;
      case 1:
	message += this.getText();
	break;
      case 2:
	this.receiveFile(username);
	break;
      default:
	return null;
    }
    }
    catch (IOException e) {e.printStackTrace();}
    return message;
  }

  private void addTime(String message) {
    long x = 0;
    long ms = 0;
    try {
      for (int i = 56; i >= 0; i -= 8) {
        x = this.is.read();
        ms += (x << i);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    TimeZone tz = TimeZone.getDefault();
    ms += tz.getDSTSavings();
    Date d = new Date(ms);
    int h = d.getHours();
    int m = d.getMinutes();
    message += "<" + Integer.toString(h) + ":" + Integer.toString(m) + ">";
    return;
  }

  private String getText() {
    int len = this.getInt();
    byte[] text = new byte[len];
    int rec = 0;
    try {
      while (rec != len) {
        rec += this.is.read(text, rec, len - rec);
      }
    } catch (IOException e) {e.printStackTrace();}
    String textString = new String(text);
    return textString;
  }

  private void receiveFile(String username) {
    String dirName = username + "_Data";
    File directory = new File(dirName);
    if (!directory.exists()) {
        directory.mkdir();
    }
    String filename = this.getText();
    File file = new File(dirName + "/" + filename);
    try {
      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      int rec = 0; //bytes received
      byte[] buf = new byte[1024]; //buffer
      int len = getInt();
      int cur = 0;
      while (cur != len) {
        rec = this.is.read(buf, 0, 1024);
	cur += rec;
	String str = new String(buf);
	char[] ch = str.toCharArray();
	fw.write(ch, 0, rec);
      }

      System.out.println("File " + filename + " received, len = " 
        + Integer.toString(len));
      fw.close(); 
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return;
  }

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
      e.printStackTrace();
    }
    return ans;
  }

}
