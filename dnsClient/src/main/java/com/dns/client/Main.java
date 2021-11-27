package com.dns.client;

import com.dns.client.console.ConsoleListener;
import com.dns.client.service.CommunicatorService;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        CommunicatorService service = new CommunicatorService("78.37.108.101");
        ConsoleListener consoleListener = new ConsoleListener(service);
        consoleListener.startListen();
    }

}
