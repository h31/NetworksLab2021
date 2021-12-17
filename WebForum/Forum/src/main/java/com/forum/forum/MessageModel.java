package com.forum.forum;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageModel {

    private String userName;
    private String message;
    private LocalDateTime dateTime;

}
