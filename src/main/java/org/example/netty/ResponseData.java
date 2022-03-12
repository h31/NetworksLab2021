package org.example.netty;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseData extends RequestData {
    private String nickname;
    private String text;
    private String time;
    private boolean fileAttach;
    private int contentLength;
    private String attName;
    private byte[] content;
}