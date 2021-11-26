public class ClientTFTP { 

  private static String address = "localhost";

  public static void main(String[] args) {
    if (args.length > 0) 
      address = args[0];
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
            HandlerRRQ handlerRRQ = new HandlerRRQ(address, spl[1]);
            handlerRRQ.handle();
          }
          break;
        case "put":
          if (spl.length != 2) {
            System.out.println("Wrong command format");
            continue;
          }
          else {
            HandlerWRQ handlerWRQ = new HandlerWRQ(address, spl[1]);
            handlerWRQ.handle();
          }
          break;
        default:
          System.out.println("unknown command " + spl[0]);
          break; 
      }
    } 
  }
}
