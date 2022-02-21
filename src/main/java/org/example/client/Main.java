package org.example.client;

public class Main {

    private static final String HOST = "pool.ntp.org";
    private static final int PORT = 123;

    public static void main(String[] args) {
        Client client = new Client(HOST, PORT);
        client.run();
    }
}