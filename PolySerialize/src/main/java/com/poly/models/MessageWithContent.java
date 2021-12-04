package com.poly.models;


import org.apache.commons.lang3.ArrayUtils;


public class MessageWithContent {

    private final Message message;
    private final byte[] content;

    public MessageWithContent(Message message, byte[] content) {
        this.message = message;
        this.content = content;
    }

    public Message getMessage() {
        return message;
    }

    public byte[] getContent() {
        return content;
    }

    public byte[] serialize() {
        byte[] messageInBytes = message.toTransferString().getBytes();
        byte[] sizeInBytes = getSizeMessage(messageInBytes.length);
        if (message.getFileName() == null && message.getFileSize() == null) {
            return ArrayUtils.addAll(sizeInBytes, messageInBytes);
        } else {
            byte[] sizeWithMessageInBytes = ArrayUtils.addAll(sizeInBytes, messageInBytes);
            return ArrayUtils.addAll(sizeWithMessageInBytes, content);
        }
    }

    public byte[] getSizeMessage(int size) {
        byte[] sizeInBytes = new byte[4];
        sizeInBytes[0] = (byte) (size >> 24);
        sizeInBytes[1] = (byte) (size >> 16);
        sizeInBytes[2] = (byte) (size >> 8);
        sizeInBytes[3] = (byte) size;
        return sizeInBytes;
    }
}
