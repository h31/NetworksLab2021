package com.example.news.services;

import com.example.news.dtos.NewsDTO;
import com.example.news.entities.News;
import com.example.news.repositories.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    public List<NewsDTO> findNews(String text) {
        List<News> result = newsRepository.findAllByText(text);
        return result.stream().map(this::createDTO).collect(Collectors.toList());
    }

    public List<String> findAllThemes() {
       return newsRepository.findAllThemes();
    }

    public List<String> findNewsByTheme(String theme) {
        return newsRepository.findNewsByTheme(theme);
    }

    public NewsDTO createNews(NewsDTO newsDTO) {
        News news = new News();
        news.setName(newsDTO.getName());
        news.setTheme(newsDTO.getTheme());
        news.setText(newsDTO.getText());
        return createDTO(newsRepository.save(news));
    }

    public NewsDTO createDTO(News news) {
        return new NewsDTO(news.getId(), news.getTheme(), news.getTheme(), news.getText());
    }
}
