package Client;

import Utils.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class ReceiveThread extends Thread {
    SocketChannel socket;

    public ReceiveThread(SocketChannel socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            byte[] bb = new byte[0];
            try {
                bb = MessageReader.readMessage(socket);
            } catch (Exception e) {
                try {
                    socket.close();
                    break;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                e.printStackTrace();
            }

            Parser p = Parser.getAll(bb);
            String userName = p.getUserName();
            char command = p.getCommand();
            String date = p.getDate();
            String text = p.getText();
            String fileName = p.getFileName();
            int fileLength = p.getFileLength();
            byte[] bytesFile = p.getFileBytes();

            if (command == Command.GREETING.getSymbol()) {
                System.out.println(
                        Color.YELLOW_BOLD_BRIGHT + userName +
                                Color.BLUE_BRIGHT + " joined the server at " +
                                Color.MAGENTA + date +
                                Color.RESET);
            }

            if (command == Command.TEXT.getSymbol()) {
                if (fileLength == 0) {
                    System.out.println(
                            Color.MAGENTA + "<" + date + "> " +
                                    Color.YELLOW_BOLD_BRIGHT + "[" + userName + "] " +
                                    Color.RESET + text);
                } else {
                    System.out.println(
                            Color.MAGENTA + "<" + date + "> " +
                                    Color.YELLOW_BOLD_BRIGHT + "[" + userName + "] " +
                                    Color.RESET + text +
                                    ", file: " + fileName);
                    try (FileOutputStream fos = new FileOutputStream(fileName +
                            new Random().nextInt(1000000))) {
                        fos.write(bytesFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (command == Command.CLOSE.getSymbol()) {
                System.out.println(
                        Utils.Color.YELLOW_BOLD_BRIGHT + userName +
                                Color.BLUE_BRIGHT + " left the server at " +
                                Color.MAGENTA + date +
                                Color.RESET);
            }
        }
    }
}
