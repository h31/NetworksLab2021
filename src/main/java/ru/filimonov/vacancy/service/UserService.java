package ru.filimonov.vacancy.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.filimonov.vacancy.dto.AuthenticationDto;
import ru.filimonov.vacancy.entity.User;
import ru.filimonov.vacancy.exception.UserAlreadyExistsException;
import ru.filimonov.vacancy.repository.UserRepository;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(AuthenticationDto user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent())
            throw new UserAlreadyExistsException();
        final var entity = new User();
        entity.setUsername(user.getUsername());
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(entity);
    }
}
