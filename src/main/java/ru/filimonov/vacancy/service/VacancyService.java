package ru.filimonov.vacancy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.filimonov.vacancy.dto.VacancyRequestDto;
import ru.filimonov.vacancy.entity.Profession;
import ru.filimonov.vacancy.entity.Vacancy;
import ru.filimonov.vacancy.exception.VacancyNotFoundException;
import ru.filimonov.vacancy.repository.ProfessionRepository;
import ru.filimonov.vacancy.repository.VacancyRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final ProfessionRepository professionRepository;

    public List<Vacancy> search(String profession, String company, String position, Integer minAge
            , Integer maxAge, Integer salary) {
        return vacancyRepository.search(profession, company, position, minAge, maxAge, salary);
    }

    public Vacancy add(VacancyRequestDto vacancy) {
        var entity = new Vacancy();
        var profession = professionRepository.findByName(vacancy.getProfession()).orElseGet(() -> {
            var it = new Profession();
            it.setName(vacancy.getProfession());
            return it;
        });
        entity.setProfession(profession);
        entity.setCompany(vacancy.getCompany());
        entity.setMinAge(vacancy.getMinAge());
        entity.setMaxAge(vacancy.getMaxAge());
        entity.setPosition(vacancy.getPosition());
        entity.setSalary(vacancy.getSalary());
        return vacancyRepository.save(entity);
    }

    public Vacancy delete(Integer id) {
        final var entity = vacancyRepository.findById(id).orElseThrow(VacancyNotFoundException::new);
        vacancyRepository.delete(entity);
        return entity;
    }
}
