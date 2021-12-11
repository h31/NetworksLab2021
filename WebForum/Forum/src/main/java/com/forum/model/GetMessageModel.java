package com.forum.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetMessageModel {

    private String mainTheme;
    private String subTheme;
    private LocalDateTime lastSeenTime;

}
