package com.poly.models;

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
}
