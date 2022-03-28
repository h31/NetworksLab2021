package ru.filimonov.vacancy.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.filimonov.vacancy.dto.ProfessionRequestDto;
import ru.filimonov.vacancy.dto.VacancyRequestDto;

import java.util.*;

@Slf4j
public class AuthorizedUser {

    private String server;
    private static final String PROFESSION_URL = "/profession/";
    private static final String VACANCY_URL = "/vacancy/";
    private final HttpHeaders headers = new HttpHeaders();
    private final RestTemplate restTemplate = new RestTemplate();

    public void startAuthorized(String server, String username, List<String> cookies) {
        this.server = server;
        parseCookie(cookies);
        System.out.println("Hello " + username + "!");
        System.out.println("Use one of these commands or if you want logout please write \"logout\"");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("find all profession - findAll," +
                    " add profession - addProfession, delete profession - deleteProfession," +
                    " search vacancy - searchVacancy, delete vacancy - deleteVacancy, add vacancy - addVacancy");
            String command = scanner.nextLine();
            switch (command) {
                case "findAll":
                    findAllProfession();
                    break;
                case "addProfession":
                    addProfession();
                    break;
                case "deleteProfession":
                    deleteProfession();
                    break;
                case "searchVacancy":
                    searchVacancy();
                    break;
                case "deleteVacancy":
                    deleteVacancy();
                    break;
                case "addVacancy":
                    addVacancy();
                    break;
                case "logout":
                    logout();
                    return;
                default:
                    System.out.println("Entered incorrectly. Enter one of this command");
                    break;
            }
        }
    }

    private void findAllProfession() {
        List<ProfessionRequestDto> professionRequestDto = new ArrayList<>();
        HttpEntity<List<ProfessionRequestDto>> request = new HttpEntity<>(professionRequestDto, headers);
        ResponseEntity response = restTemplate.exchange(server + PROFESSION_URL, HttpMethod.GET, request, List.class);
        List<LinkedHashMap<String, String>> professions = (ArrayList) response.getBody();
        if (professions == null) {
            System.out.println("Something went wrong, try again later");
        } else {
            professions.forEach(System.out::println);
        }
    }

    private void addProfession() {
        System.out.println("Please write the name of the profession you want to add");
        Scanner scanner = new Scanner(System.in);
        String profession = scanner.nextLine();
        ProfessionRequestDto professionRequestDto = new ProfessionRequestDto();
        HttpEntity<ProfessionRequestDto> request = new HttpEntity<>(professionRequestDto, headers);
        String uri = server + PROFESSION_URL + "?profession=" + profession;
        ResponseEntity response = restTemplate.exchange(uri, HttpMethod.POST, request, ProfessionRequestDto.class);
        System.out.println("Profession has been  added " + response.getBody());
    }

    private void deleteProfession() {
        System.out.println("Please write the id of the profession you want to delete");
        Scanner scanner = new Scanner(System.in);
        String profession = scanner.nextLine();
        ProfessionRequestDto professionRequestDto = new ProfessionRequestDto();
        HttpEntity<ProfessionRequestDto> request = new HttpEntity<>(professionRequestDto, headers);
        String uri = server + PROFESSION_URL + profession;
        try {
            ResponseEntity response = restTemplate.exchange(uri, HttpMethod.DELETE, request, ProfessionRequestDto.class);
            System.out.println("Profession has been removed " + response.getBody());
        } catch (HttpClientErrorException.BadRequest badRequest) {
            System.out.println("The profession has not been deleted, you entered a non-numeric value");
        }
    }

    private void searchVacancy() {
        System.out.println("Please write search parameter");
        System.out.println("If you do not want some parameter to participate in the search enter \"$\"");

        Scanner scanner = new Scanner(System.in);
        Map<String, String> searchValues = new HashMap<>();
        System.out.println("Please write profession");
        searchValues.put("profession", scanner.nextLine());
        System.out.println("Please write company");
        searchValues.put("company", scanner.nextLine());
        System.out.println("Please write position");
        searchValues.put("position", scanner.nextLine());
        System.out.println("Please write minAge");
        searchValues.put("minAge", scanner.nextLine());
        System.out.println("Please write maxAge");
        searchValues.put("maxAge", scanner.nextLine());
        System.out.println("Please write salary");
        searchValues.put("salary", scanner.nextLine());

        StringBuilder uri = new StringBuilder(server + VACANCY_URL + "/search" + "?");
        searchValues.forEach((key, value) -> {
            if (!value.equals("$")) {
                uri.append(key).append("=").append(value).append("&");
            }
        });

        List<VacancyRequestDto> vacancyRequestDTOS = new ArrayList<>();
        HttpEntity<List<VacancyRequestDto>> request = new HttpEntity<>(vacancyRequestDTOS, headers);
        ResponseEntity response = restTemplate.exchange(uri.toString(), HttpMethod.GET, request, List.class);
        List<LinkedHashMap<String, String>> vacancies = (ArrayList) response.getBody();
        if (vacancies == null) {
            System.out.println("Something went wrong, try again later");
        } else {
            vacancies.forEach(System.out::println);
        }
    }

    private void deleteVacancy() {
        System.out.println("Please write the id of the vacancy you want to delete");
        Scanner scanner = new Scanner(System.in);
        int vacancy = scanner.nextInt();
        VacancyRequestDto vacancyRequestDto = new VacancyRequestDto();
        HttpEntity<VacancyRequestDto> request = new HttpEntity<>(vacancyRequestDto, headers);
        String uri = server + VACANCY_URL + vacancy;
        ResponseEntity response = restTemplate.exchange(uri, HttpMethod.DELETE, request, VacancyRequestDto.class);
        System.out.println("Vacancy has been removed " + response.getBody());
    }

    private void addVacancy() {

    }

    private void logout() {
        restTemplate.getForObject(server + "/logout", String.class);
    }

    public void parseCookie(List<String> cookies) {
        StringBuilder jSession = new StringBuilder();
        cookies.forEach(cookie -> {
            String[] splitCookie = cookie.split(";");
            jSession.append(splitCookie[0]);
        });
        headers.add("Cookie", jSession.toString());
    }
}
