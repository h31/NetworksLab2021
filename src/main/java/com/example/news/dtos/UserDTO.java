package com.example.news.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @NotNull
    private String username;

    @NotNull
    private String password;

    private String token;

    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
