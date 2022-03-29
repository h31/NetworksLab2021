package com.example.news.client;

public class Main {
    private static final String SERVER = "http://localhost:8085";
    public static void main(String[] args) {
        new Client().startConnection(SERVER);
    }
}
