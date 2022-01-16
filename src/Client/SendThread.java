package Client;

import Utils.Color;
import Utils.Command;
import Utils.MessageCreator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SendThread extends Thread {
    SocketChannel socketChannel;
    String userName;

    public SendThread(SocketChannel socket, String userName) {
        this.socketChannel = socket;
        this.userName = userName;
    }

    @Override
    public void run() {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        byte[] bytes = MessageCreator.createMessage(userName, Command.GREETING.getSymbol());
        try {
            socketChannel.write(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            String text = null;

            try {
                do {
                    text = consoleReader.readLine();
                } while (text.equals(""));
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean end = false;

            if (text.contains("///")) {
                String splittedText = "";
                try {
                    splittedText = text.split("///")[1].trim();
                } catch (Exception ignored) { }
                if (splittedText.equals("close")) {
                    bytes = MessageCreator.createMessage(userName, Command.CLOSE.getSymbol());
                    end = true;
                } else {
                    byte[] fileBytes = new byte[0];
                    String fileName = "";
                    try {
                        fileName = getFileName(splittedText);
                        File file = new File(splittedText);
                        fileBytes = Files.readAllBytes(file.toPath());
                    } catch (IOException e) {
                        System.out.println(Color.RED + "no such file" + Color.RESET);
                    }
                    bytes = MessageCreator.createMessage(userName, Command.TEXT.getSymbol(), "",
                            fileName, fileBytes.length, fileBytes, text.split("///")[0]);
                }
            } else {
                bytes = MessageCreator.createMessage(userName, Command.TEXT.getSymbol(), text, "");
            }

            try {
                socketChannel.write(ByteBuffer.wrap(bytes));
                if (end) break;
            } catch (IOException e) {
                try {
                    socketChannel.close();
                    break;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }


    public String getFileName(String filePath) {
        return Paths.get(filePath).getFileName().toString();
    }

}