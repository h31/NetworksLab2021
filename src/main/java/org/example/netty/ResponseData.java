package org.example.netty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseData {
    private int intValue;
    private String nickname;
    private String text;
    private String time;
}