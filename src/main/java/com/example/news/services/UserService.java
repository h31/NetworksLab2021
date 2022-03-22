package com.example.news.services;

import com.example.news.config.jwt.JWTUtil;
import com.example.news.dtos.UserDTO;
import com.example.news.entities.User;
import com.example.news.repositories.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UsersRepository usersRepository;
    private final UserDetailService userDetailService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserDTO registryUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return createDTO(usersRepository.save(user));
    }

    public UserDTO jwtLogin(UserDTO userDTO) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDTO.getUsername(),
                userDTO.getPassword()));
        User userDetails =
                (User) userDetailService.loadUserByUsername(userDTO.getUsername());

        String jwtToken = jwtUtil.generateToken(userDetails);
        UserDTO response = new UserDTO();
        response.setToken(jwtToken);
        return response;
    }

    public UserDTO createDTO(User user) {
        return new UserDTO(user.getUsername(), user.getPassword());
    }
}
