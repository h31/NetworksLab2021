package ru.filimonov.vacancy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.filimonov.vacancy.entity.Profession;
import ru.filimonov.vacancy.service.ProfessionService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/profession")
public class ProfessionController {

    private final ProfessionService professionService;

    @GetMapping("/")
    public List<Profession> findAll() {
        return professionService.findAll();
    }

    @PostMapping("/")
    public Profession add(String profession) {
        return professionService.add(profession);
    }

    @DeleteMapping("/{id}")
    public Profession delete(@PathVariable Integer id) {
        return professionService.delete(id);
    }
}
