package ru.filimonov.vacancy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.filimonov.vacancy.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
