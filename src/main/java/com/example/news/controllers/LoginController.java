package com.example.news.controllers;

import com.example.news.dtos.UserDTO;
import com.example.news.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;

    @PostMapping("/registry")
    public ResponseEntity<UserDTO> registry(@Validated @RequestBody UserDTO userDTO){
        return new ResponseEntity<>(userService.registryUser(userDTO), HttpStatus.OK);
    }

}
