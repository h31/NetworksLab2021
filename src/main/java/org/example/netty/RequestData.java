package org.example.netty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestData {
    private String nickName;
    private String text;
    private boolean fileAttach;
    private int contentLength;
    private byte[] content;
}
