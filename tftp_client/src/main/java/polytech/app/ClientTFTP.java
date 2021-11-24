public class ClientTFTP { 

  public static void main(String[] args) {
    if (args.length > 0) {
      try {
        InetAddress address = InetAddress.getByName(adr);
      }   
      catch () {}
    }
    while (true) {
      String cmd = System.console().readLine();
      cmd = cmd.trim();
      String[] spl = cmd.split(" ");
      spl[0] = spl[0].toLowerCase();
      switch (spl[0]) {
        case "q": return;
        case "quit": return;
        case "get": 
          if (spl.length != 2) {
            System.out.println("Wrong command format");
            continue;
          }
          else {
            HandlerRRQ handler = new HandlerRRQ(address, spl[1]);
            byte[] rrq = HandlerAssistant.getRequest((byte)1, spl[1], "octet");
            HandlerAssistant.sendPacket()
            handler.handle();
          }
          break;
        case "put":
          if (spl.length != 2) {
            System.out.println("Wrong command format");
            continue;
          }
          break;
        default:
          System.out.println("unknown command " + spl[0]);
          break; 
      }
    } 
  }
}
