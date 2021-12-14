package com.example.delservice.controller;

import com.example.delservice.model.User;
import com.example.delservice.repository.UserRepository;
import com.example.delservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;



    @PostMapping("/register")
    void register(
            @RequestParam("username") final String username,
            @RequestParam("password") final String password) {

        boolean status = userService
                .saveUser(new User(username, password));

        if(!status) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exist");
        }
    }

    @GetMapping("/login")
    void login() {
        throw new ResponseStatusException(HttpStatus.OK, "Login success");
    }

}
