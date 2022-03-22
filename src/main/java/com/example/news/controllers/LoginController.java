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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;

    @PostMapping("/registration")
    public ResponseEntity<UserDTO> registry(@Validated @RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(userService.registryUser(userDTO), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> handleLogin(@RequestBody UserDTO userDTO,
                                               HttpServletResponse httpServletResponse) {
        UserDTO loginResponse = userService.jwtLogin(userDTO);
        Cookie cookie = new Cookie("token", loginResponse.getToken());
        httpServletResponse.addCookie(cookie);
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }
}
