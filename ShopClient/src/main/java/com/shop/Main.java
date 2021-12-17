package com.shop;

import com.shop.cli.ConsoleInterface;
import com.shop.model.Good;
import com.shop.rest.RestService;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        ConsoleInterface consoleInterface = new ConsoleInterface();
        consoleInterface.run();
    }

}
