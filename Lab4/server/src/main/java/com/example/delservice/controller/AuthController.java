package com.example.delservice.controller;

import com.example.delservice.model.User;
import com.example.delservice.repository.UserRepository;
import com.example.delservice.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;


    @ApiOperation(
            value = "Регистрация пользователя",
            notes = "Позволяет зарегистрировать пользователя"
    )

    @PostMapping("/register")
    void register(
            @RequestParam("username") @ApiParam(value = "Имя пользователя", example = "vasya") String username,
            @RequestParam("password") @ApiParam(value = "Пароль", example = "3530901") String password) {

        boolean status = userService
                .saveUser(new User(username, password));

        if (!status) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exist");
        }
    }

    @ApiOperation(
            value = "Валидация пользователя",
            notes = "Метод для получения клиента информации о правах текущего пользователя" +
                    "Возвращает boolean переменную. " +
                    "true = если у пользователя права ROLE_SELLER, " +
                    "false если права обычного пользователя"
    )
    @GetMapping("/login")
    boolean login() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean hasUserRole = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_SELLER"));

        throw new ResponseStatusException(HttpStatus.OK, String.valueOf(hasUserRole));

    }

}
