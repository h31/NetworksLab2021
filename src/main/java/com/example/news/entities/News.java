package com.example.news.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@Table(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "theme")
    private String theme;

    @NotNull
    @Column(name = "name")
    private String name;

    @Nullable
    @Column(name = "text")
    private String text;
}
