package com.example.news.client;

import com.example.news.dtos.UserDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Scanner;

public class Client {
    private String server;
    private static final String REGISTRATION_URL = "/registration";
    private static final String LOGIN_URL = "/login";
    private static final RestTemplate restTemplate = new RestTemplate();

    public void startConnection(String server) {
        this.server = server;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Для регистрации введите \"registration\", для входа в аккаунт - \"login\"" +
                    ", для выхода из приложения - \"exit\"");
            String hello = scanner.nextLine();
            switch (hello) {
                case "login":
                    login();
                    break;
                case "registration":
                    registration();
                    break;
                case "exit":
                    return;
                default:
                    System.out.println("Некорректный ввод");
                    break;
            }
        }
    }

    public void registration() {
        UserDTO requestDTO = new UserDTO();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите имя пользователя: ");
        String username = scanner.nextLine();
        requestDTO.setUsername(username);
        System.out.println("Введите пароль: ");
        String password = scanner.nextLine();
        requestDTO.setPassword(password);
        HttpEntity<UserDTO> request = new HttpEntity<>(requestDTO);
        String url = this.server + REGISTRATION_URL;
        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.POST, request, UserDTO.class);
            System.out.println("Пользователь зарегистрирован");
            login();
        } catch (HttpClientErrorException e) {
            System.out.println("Пользователь уже существует");
        }
    }

    public void login() {
        UserDTO requestDTO = new UserDTO();
        HttpEntity<UserDTO> request = new HttpEntity<>(requestDTO);
        Scanner scanner = new Scanner(System.in);
        String url = this.server + LOGIN_URL;
        try {
            System.out.println("Введите имя пользователя: ");
            String username = scanner.nextLine();
            requestDTO.setUsername(username);
            System.out.println("Введите пароль: ");
            String password = scanner.nextLine();
            requestDTO.setPassword(password);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class);
            HttpHeaders headers = response.getHeaders();
            List<String> cookies = headers.get("Set-Cookie");
            System.out.println("Успешный вход");
            new AuthorizedUser().startAuthorized(server, requestDTO.getUsername(), cookies);
        } catch (HttpClientErrorException e) {
            System.out.println("Неверное имя пользователя или пароль");
        }
    }
}
