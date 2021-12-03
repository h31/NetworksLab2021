package org.example.client;

public class MainClient {
    public static void main(String[] args) {
        Client client = new Client();
        client.startConnection("localhost", 5555);

    }
}
