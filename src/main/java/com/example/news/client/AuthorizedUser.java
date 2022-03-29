package com.example.news.client;

import com.example.news.dtos.NewsDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Scanner;

public class AuthorizedUser {

    private String server;
    private static final String NEWS_URL = "/api/v1/news";
    private final HttpHeaders headers = new HttpHeaders();
    private final RestTemplate restTemplate = new RestTemplate();
    private final Scanner scanner = new Scanner(System.in);

    public void startAuthorized(String server, String username, List<String> cookies) {
        this.server = server;
        parseCookie(cookies);
        System.out.println("Привет, " + username + "!");
        System.out.println("Для просмотра команд введите \"help\"");
        while (true) {
            String command = scanner.nextLine();
            switch (command) {
                case "find" -> find();
                case "themes" -> themes();
                case "newsByTheme" -> newsByTheme();
                case "create" -> create();
                case "help" -> System.out.println("Поиск новостей по тексту - find," +
                        " вывести все темы - themes, поиск новостей по теме - newsByTheme," +
                        " создать новость - create, выйти из аккаунта - logout");
                case "logout" -> {
                    logout();
                    return;
                }
                default -> System.out.println("Некорректный ввод. Для отображения возможных команд введите \"help\"");
            }
        }
    }

    private void find() {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        System.out.println("Введите текст для поиска");
        String text = scanner.nextLine();
        ResponseEntity response = restTemplate.exchange(
                String.format("%s%s/find?text=%s", server, NEWS_URL, text), HttpMethod.GET, request, List.class);
        List<NewsDTO> news = (List<NewsDTO>) response.getBody();
        if (news == null) {
            System.out.println("Что-то пошло не так. Попробуйте позже");
        } else {
            System.out.println(news);
        }
    }

    private void themes() {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        ResponseEntity response = restTemplate.exchange(
                String.format("%s%s/findAllThemes", server, NEWS_URL), HttpMethod.GET, request, List.class);
        List<String> news = (List<String>) response.getBody();
        if (news == null) {
            System.out.println("Что-то пошло не так. Попробуйте позже");
        } else {
            System.out.println(news);
        }
    }

    private void newsByTheme() {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        System.out.println("Введите тему для поиска");
        String text = scanner.nextLine();
        ResponseEntity response = restTemplate.exchange(
                String.format("%s%s/findNewsByTheme?theme=%s", server, NEWS_URL, text), HttpMethod.GET, request, List.class);
        List<NewsDTO> news = (List<NewsDTO>) response.getBody();
        if (news == null) {
            System.out.println("Что-то пошло не так. Попробуйте позже");
        } else {
            System.out.println(news);
        }
    }

    private void create() {
        NewsDTO news = new NewsDTO();
        System.out.println("Введите название новости");
        news.setName(scanner.nextLine());
        System.out.println("Введите тему новости");
        news.setTheme(scanner.nextLine());
        System.out.println("Введите текст новости");
        news.setText(scanner.nextLine());
        HttpEntity<NewsDTO> request = new HttpEntity<>(news, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                String.format("%s%s/createNew", server, NEWS_URL), HttpMethod.POST, request, String.class);
        System.out.println(response.getBody());
    }

    private void logout() {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        restTemplate.exchange(server + "/logout", HttpMethod.GET, request, String.class);
    }

    public void parseCookie(List<String> cookies) {
        StringBuilder token = new StringBuilder();
        cookies.forEach(cookie -> {
            String[] splitCookie = cookie.split(";");
            token.append(splitCookie[0]);
        });
        headers.add("Cookie", token.toString());
    }
}
