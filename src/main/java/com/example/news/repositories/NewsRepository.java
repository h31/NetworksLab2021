package com.example.news.repositories;

import com.example.news.entities.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    @Query(value =
            "SELECT nw FROM News as nw WHERE nw.text LIKE %?1%")
    List<News> findAllByText(String text);

    @Query(value = "SELECT theme FROM News")
    List<String> findAllThemes();

    @Query(value = "SELECT text FROM News WHERE theme = ?1")
    List<String> findNewsByTheme(String theme);
}
