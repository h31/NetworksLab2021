import java.io.InputStream;
import java.io.IOException;
import java.util.TimeZone;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;

class MyProtocolClient {
  private InputStream is;
  private int hour;
  private int min;
  private String myUsername;
  private String username = null;
  private String text = null;
  private String filename = null;

  public MyProtocolClient (InputStream is, String myUsername) {
    this.is = is;
    this.myUsername = myUsername;
  }

  public void decodeMessage() {
    try {
    int type = is.read(); //type
    this.addTime(); //time
    this.username = this.getText(); //username
    switch (type) {
      case 0:
	this.text = this.getText(); //text
	this.receiveFile(); //file
	System.out.println(String.format("<%d:%d> [%s] %s (%s attached)", 
	  this.hour, this.min, this.username, this.text, this.filename));
        break;
      case 1:
	this.text = this.getText(); //text
	System.out.println(String.format("<%d:%d> [%s] %s",
	  this.hour, this.min, this.username, this.text));
	break;
      case 2:
	this.receiveFile(); //file
        System.out.println(String.format("<%d:%d> [%s] (%s attached)",
	  this.hour, this.min, this.username, this.filename));
	break;
      default:
	return;
    }
    }
    catch (IOException e) {e.printStackTrace();}
  }

  private void addTime() {
    long x = 0;
    long ms = 0;
    try {
      for (int i = 56; i >= 0; i -= 8) {
        x = this.is.read();
	if (x == -1) {
	  System.out.println("Server lost");
	  System.exit(-1);
	}
        ms += (x << i);
      }
    }
    catch (IOException e) {
      System.out.println("Server lost");
      System.exit(-1);
    }
    TimeZone tz = TimeZone.getDefault();
    ms += tz.getDSTSavings();
    Date d = new Date(ms);
    this.hour = d.getHours();
    this.min = d.getMinutes();
    return;
  }

  private String getText() {
    int len = this.getInt();
    byte[] bytes = new byte[len];
    int rec = 0;
    try {
      while (rec != len) {
        bytes[rec]= (byte)this.is.read();
	if (bytes[rec] == -1) {
	  System.out.println("Server lost");
          System.exit(-1);
	}
	rec++;
      }
    }
    catch (IOException e) {
      System.out.println("Server lost");
      System.exit(-1);
    }
    String textString = new String(bytes);
    return textString;
  }

  private void receiveFile() {
    String dirName = this.myUsername + "_Data";
    File directory = new File(dirName);
    if (!directory.exists()) {
        directory.mkdir();
    }
    this.filename = this.getText();
    File file = new File(dirName + "/" + filename);
    try {
      FileOutputStream os = new FileOutputStream(file);
      int len = this.getInt();
      System.out.println("DEBUG_LEN:" + len);
      int rec = 0;
      byte b;
      while (rec != len) {
        b = (byte)this.is.read();
	if (b == -1) {
	  System.out.println("b == -1");
	  System.out.println("rec=" + rec);
	  System.out.println("Server lost");
          System.exit(-1);
	}
	os.write(b);
	rec++;
      } 
      os.close();
    }
    catch (IOException e) {
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
	if (x == -1) {
	  System.out.println("Server lost");
          System.exit(-1);
	}
        ans += (x << i);
      }
    }
    catch (IOException e) {
      System.out.println("Server lost");
      System.exit(-1);
    }
    return ans;
  }
}
