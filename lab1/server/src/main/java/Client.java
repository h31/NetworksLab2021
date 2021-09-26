import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner scanner;



    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.startConnection(null, 6666);
           String resp = client.sendMessage();
            System.out.println(resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        scanner = new Scanner(System.in);
    }

    public String sendMessage() throws IOException {
        /*out.println(msg);*/
        String scanned;
        System.out.print("Введите текст: ");
        while ((scanned = scanner.nextLine()) != null) {
            if (".".equals(scanned)) {
                System.out.println("client finished typing");
                out.println("bye from client");
                break;
            }
            out.println(scanned);
            System.out.println(in.readLine());
            System.out.print("Введите текст: ");
        }

        String resp = "safuiufsaio";
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}
