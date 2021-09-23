package org.example;

import java.io.IOException;

public class MainClient {
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.startConnection("127.0.0.1", 5555);

    }
}
