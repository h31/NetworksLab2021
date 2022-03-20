package com.example.news.services;

import com.example.news.dtos.UserDTO;
import com.example.news.entities.User;
import com.example.news.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UsersRepository usersRepository;

    public UserDTO registryUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin());
        user.setPassword(userDTO.getPassword());
        return createDTO(usersRepository.save(user));
    }

    public UserDTO createDTO(User user) {
        return new UserDTO(user.getLogin(), user.getPassword());
    }
}
