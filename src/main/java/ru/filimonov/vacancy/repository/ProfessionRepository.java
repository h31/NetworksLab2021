package ru.filimonov.vacancy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.filimonov.vacancy.entity.Profession;

import java.util.Optional;

public interface ProfessionRepository extends JpaRepository<Profession, Integer> {
    Optional<Profession> findByName(String name);
}
