import java.util.Date;
import java.io.InputStream;
import java.io.IOException;


class MyByteArray {
  int pos;
  int len;
  byte[] arr;

  public MyByteArray (int len) {
    this.pos = 0;
    this.len = len;
    this.arr = new byte[len];
  }

  public void addInt(int n) {
    for (int i = 24; i >= 0; i -= 8)
      add((byte)(n >> i));
    return;
  }

  public void addTime() {
    Date d = new Date();
    long time = d.getTime();
    for (int i = 56; i >= 0; i -= 8)
      add((byte)(time >> i));
    return;
  }

  public void addString(String str) {
    byte[] bytes = str.getBytes();
    this.addInt(bytes.length);
    this.addAll(bytes);
    return;
  }

  public int receiveFile(int fileLen, InputStream is) {
    addInt(fileLen);
    int rec = 0; //bytes received
    byte b;
    try {
      while (rec != fileLen) {
        b = (byte)is.read();
	rec++;
        this.add(b);
      }
    System.out.println("RECEIVED = " + rec); 
    } catch (IOException e) {return -1;}
    return 0;
  }

  public void add(byte data) {
    if (this.pos == this.len - 1)
      expandArray();
    this.arr[pos] = data;
    this.pos++;
    return;
  }

  private void addAll(byte[] data) {
    for (int i = 0; i < data.length; i++)
      this.add(data[i]);
    return;
  }

  private void expandArray() {
    int newLen = (new Double(this.len * 1.5 + 1).intValue());
    byte[] newArray = new byte[newLen];
    for (int i = 0; i <= this.pos; i++)
      newArray[i] = this.arr[i];
    this.len = newLen;
    this.arr = newArray;
    return;
  }

  public byte[] getArray() {
    byte[] newArray = new byte[pos];
    for (int i = 0; i < this.pos; i++) 
      newArray[i] = this.arr[i];
    return newArray;
  }
}
