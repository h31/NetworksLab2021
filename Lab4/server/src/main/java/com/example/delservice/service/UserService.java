package com.example.delservice.service;

import com.example.delservice.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    public boolean saveUser(User user);

}
