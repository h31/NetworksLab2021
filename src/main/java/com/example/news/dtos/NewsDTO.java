package com.example.news.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class NewsDTO {
    private Long id;

    @NotNull
    private String theme;

    @NotNull
    private String name;

    private String text;
}
