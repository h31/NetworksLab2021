package com.example.news.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class NewsDTO {
    private Long id;

    private String theme;

    private String name;

    private String text;
}
