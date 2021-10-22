package org.example;

import lombok.Data;

@Data
public class Message {
    private String userName;
    private String text;
    private String attachName;
    private byte[] attachment;
    private int attachSize;

}
