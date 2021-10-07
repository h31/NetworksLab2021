package com.poly.server.threads;

import com.poly.models.MessageWithContent;
import com.poly.sockets.MessageReader;
import com.poly.sockets.MessageWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ClientThread extends Thread {

    private MessageReader messageReader;
    private MessageWriter messageWriter;
    private List<MessageWriter> writers;

    public ClientThread(InputStream inputStream, OutputStream outputStream, List<MessageWriter> writers) {
        this.messageReader = new MessageReader(inputStream);
        this.messageWriter = new MessageWriter(outputStream);
        this.writers = writers;
    }

    @Override
    public void run() {
        try {
            writers.add(messageWriter);
            MessageWithContent message = null;
            while (true) {
                try {
                    if (messageReader.readyForMessageReading()) {
                        message = messageReader.read();
                        System.out.println("READED");
                    }
                    if (message != null) {
                        for (MessageWriter writer : writers) {
                            message.getMessage().setDate((LocalDate.now().toString() + " " + LocalTime.now().toString()).replace(":", "."));
                            writer.write(message);
                        }
                        message = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                messageReader.close();
                messageWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writers.remove(messageWriter);
        }
    }
}
