package ru.filimonov.vacancy.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.filimonov.vacancy.dto.AuthenticationDto;
import ru.filimonov.vacancy.entity.User;
import ru.filimonov.vacancy.service.UserService;

@Validated
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/registration")
    public User registration(AuthenticationDto user) {
        return userService.register(user);
    }

    @ApiOperation("Login.")
    @PostMapping("/login")
    public void fakeLogin(@RequestParam String username, @RequestParam String password) {
        throw new IllegalStateException("This method shouldn't be called. It's implemented by Spring Security filters.");
    }

    @ApiOperation("Logout.")
    @PostMapping("/logout")
    public void fakeLogout() {
        throw new IllegalStateException("This method shouldn't be called. It's implemented by Spring Security filters.");
    }
}
