package com.forum.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageForSave {

    private String userName;
    private String message;
    private String mainThemeName;
    private String subThemeName;

}
