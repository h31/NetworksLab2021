package com.poly.server.threads;

import com.poly.models.MessageWithContent;
import com.poly.sockets.MessageReader;
import com.poly.sockets.MessageWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger LOG = LoggerFactory.getLogger(ClientThread.class);

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
                        LOG.debug("Message {} was readed", message.toString());
                        for (MessageWriter writer : writers) {
                            message.getMessage().setDate((LocalDate.now().toString() + " " + LocalTime.now().toString()).replace(":", "."));
                            writer.write(message);
                        }
                        LOG.debug("Messages were written");
                    }
                } catch (IOException e) {
                    try {
                        onQuit();
                    } catch (IOException ex) {
                        LOG.error("Exception when trying to close IOStreams");
                    }
                }
            }
        } finally {
            try {
                onQuit();
            } catch (IOException e) {
                LOG.error("Exception when trying to close IOStreams");
            }
        }
    }

    private void onQuit() throws IOException {
        writers.remove(messageWriter);
        messageReader.close();
        messageWriter.close();
    }
}
