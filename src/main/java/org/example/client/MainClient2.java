package org.example.client;

public class MainClient2 {
    public static void main(String[] args) {
        Client client = new Client();
        client.startConnection("127.0.0.1", 5555);
    }
}
