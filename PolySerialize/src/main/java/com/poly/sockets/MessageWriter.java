package com.poly.sockets;

import com.poly.models.Message;
import com.poly.models.MessageWithContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MessageWriter {

    private final DataOutputStream outputStream;
    private final static Logger LOG = LoggerFactory.getLogger(MessageWriter.class);

    public MessageWriter(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(outputStream);
    }

    private void writeMessage(Message message) {
        String strMessage = message.toTransferString();
        try {
            byte[] byteMessage = strMessage.getBytes();
            outputStream.write(byteMessage.length >> 24);
            outputStream.write(byteMessage.length >> 16);
            outputStream.write(byteMessage.length >> 8);
            outputStream.write(byteMessage.length);
            outputStream.write(byteMessage);
        } catch (IOException e) {
            LOG.error("Exception when writing to OutputStream");
        }
    }

    private void writeFile(byte[] file) throws IOException {
        outputStream.write(file);
        outputStream.flush();
    }

    public void close() throws IOException {
        outputStream.close();
    }

    public void write(MessageWithContent messageWithContent) throws IOException {
        Message message = messageWithContent.getMessage();
        writeMessage(message);
        if(message.getFileSize() != null && message.getFileSize() > 0
            && message.getFileName() != null && !message.getFileName().isEmpty()) {
            writeFile(messageWithContent.getContent());
        }
    }


}
