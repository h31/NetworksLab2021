package com.poly.sockets;

import com.poly.models.Message;
import com.poly.models.MessageWithContent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MessageWriter {

    private final DataOutputStream outputStream;

    public MessageWriter(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(outputStream);
    }

    private void writeMessage(Message message) {
        String strMessage = message.toTransferString();
        try {
            outputStream.write(strMessage.getBytes().length / (int) Math.pow(2, 24));
            outputStream.write(strMessage.getBytes().length / (int) Math.pow(2, 16));
            outputStream.write(strMessage.getBytes().length / (int) Math.pow(2, 8));
            outputStream.write(strMessage.getBytes().length % (int) Math.pow(2, 8));
            outputStream.write(strMessage.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
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
