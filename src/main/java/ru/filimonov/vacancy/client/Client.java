package ru.filimonov.vacancy.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.filimonov.vacancy.dto.AuthenticationDto;
import ru.filimonov.vacancy.exception.UserAlreadyExistsException;

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
            System.out.println("Would you like to login or register?\nEnter \"login\" or \"registration\" or maybe \"exit\"");
            String hello = scanner.nextLine();
            if (hello.equals("login")) {
                login();
            } else if (hello.equals("registration")) {
                registration();
            } else if (hello.equals("exit")) {
                return;
            } else {
                System.out.println("Entered incorrectly. Enter \"login\" or \"registration\"");
            }
        }
    }

    public void registration() {
        AuthenticationDto requestDTO = new AuthenticationDto();
        Scanner scanner = new Scanner(System.in);
        System.out.println("please enter username: ");
        String username = scanner.nextLine();
        requestDTO.setUsername(username);
        System.out.println("please enter password: ");
        String password = scanner.nextLine();
        requestDTO.setPassword(password);
        HttpEntity<AuthenticationDto> request = new HttpEntity<>(requestDTO);
        String url = this.server + REGISTRATION_URL;
        while (true) {
            try {
                ResponseEntity response = restTemplate.exchange(
                        url + "?username=" + requestDTO.getUsername() + "&password=" + requestDTO.getPassword(),
                        HttpMethod.POST, request, AuthenticationDto.class);
                System.out.println("Successfully registration: " + response.getBody());
                login();
                return;
            } catch (UserAlreadyExistsException e) {
                System.out.println("User with this name already exists");
            }
        }
    }

    public void login() {
        AuthenticationDto requestDTO = new AuthenticationDto();
        HttpEntity<AuthenticationDto> request = new HttpEntity<>(requestDTO);
        Scanner scanner = new Scanner(System.in);
        String url = this.server + LOGIN_URL;
        while (true) {
            try {
                System.out.println("please enter username: ");
                String username = scanner.nextLine();
                requestDTO.setUsername(username);
                System.out.println("please enter password: ");
                String password = scanner.nextLine();
                requestDTO.setPassword(password);
                ResponseEntity response = restTemplate.exchange(
                        url + "?username=" + requestDTO.getUsername() + "&password=" + requestDTO.getPassword(),
                        HttpMethod.POST, request, AuthenticationDto.class);
                HttpHeaders headers = response.getHeaders();
                List<String> cookies = headers.get("Set-Cookie");
                System.out.println("Successfully login");
                new AuthorizedUser().startAuthorized(server, requestDTO.getUsername(), cookies);
                return;
            } catch (HttpClientErrorException.Unauthorized e) {
                System.out.println("Unauthorized");
                System.out.println("If you want registration, just enter registration else write login");
                String command = scanner.nextLine();
                if (command.equals("registration")) {
                    return;
                } else {
                    System.out.println("Try again");
                }
            }
        }
    }
}
