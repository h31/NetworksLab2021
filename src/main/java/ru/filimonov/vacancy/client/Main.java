package ru.filimonov.vacancy.client;

public class Main {
    private static final String SERVER = "http://localhost:8090";
    public static void main(String[] args) {
        new Client().startConnection(SERVER);
    }
}
