package com.example.news.repositories;

import com.example.news.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);

    Optional<Boolean> existsByUsername(String username);
}
