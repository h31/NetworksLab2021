package com.forum.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GetMessageModel {

    private String mainTheme;
    private String subTheme;
    private LocalDateTime lastSeenTime;

}
