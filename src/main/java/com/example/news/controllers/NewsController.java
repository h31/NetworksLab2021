package com.example.news.controllers;


import com.example.news.dtos.NewsDTO;
import com.example.news.services.NewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Api("NewsController")
@RequestMapping("/api/v1/news")
public class NewsController {
    private final NewsService newsService;

    @ApiOperation(
            value = "all news",
            notes = "finds all news that contain the text specified in the parameter"
    )
    @GetMapping("/find")
    public ResponseEntity<List<NewsDTO>> findNews(@RequestParam String text) {
        return new ResponseEntity<>(newsService.findNews(text), HttpStatus.OK);
    }

    @ApiOperation(
            value = "all themes",
            notes = "find all themes in db"
    )
    @GetMapping("/findAllThemes")
    public ResponseEntity<List<String>> findAllThemes() {
        return new ResponseEntity<>(newsService.findAllThemes(), HttpStatus.OK);
    }

    @ApiOperation(
            value = "all news by theme",
            notes = "find all news by theme in db"
    )
    @GetMapping("/findNewsByTheme")
    public ResponseEntity<List<String>> findNewsByTheme(@RequestParam String theme) {
        return new ResponseEntity<>(newsService.findNewsByTheme(theme), HttpStatus.OK);
    }

    @ApiOperation(
            value = "create new",
            notes = "create new with some parameters"
    )
    @PostMapping("/createNew")
    public ResponseEntity<NewsDTO> createNew(@Validated @RequestBody NewsDTO newsDTO) {
        return new ResponseEntity<>(newsService.createNews(newsDTO), HttpStatus.OK);
    }
}
