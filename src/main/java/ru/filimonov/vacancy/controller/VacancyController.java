package ru.filimonov.vacancy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.filimonov.vacancy.dto.VacancyRequestDto;
import ru.filimonov.vacancy.entity.Vacancy;
import ru.filimonov.vacancy.service.VacancyService;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/vacancy")
public class VacancyController {

    private final VacancyService vacancyService;

    @GetMapping("/search")
    public List<Vacancy> search(@RequestParam(required = false) String profession, @RequestParam(required = false) String company,
                                @RequestParam(required = false) String position, @RequestParam(required = false) Integer minAge,
                                @RequestParam(required = false) Integer maxAge, @RequestParam(required = false) Integer salary) {
        return vacancyService.search(profession, company, position, minAge, maxAge, salary);
    }

    @PostMapping("/")
    public Vacancy add(@Validated VacancyRequestDto vacancy) {
        return vacancyService.add(vacancy);
    }

    @DeleteMapping("/{id}")
    public Vacancy delete(@Valid @PathVariable Integer id) {
        return vacancyService.delete(id);
    }
}
