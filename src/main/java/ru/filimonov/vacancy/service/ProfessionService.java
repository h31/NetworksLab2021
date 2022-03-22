package ru.filimonov.vacancy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.filimonov.vacancy.entity.Profession;
import ru.filimonov.vacancy.exception.ProfessionNotFoundException;
import ru.filimonov.vacancy.repository.ProfessionRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfessionService {

    private final ProfessionRepository professionRepository;

    public List<Profession> findAll() {
        return professionRepository.findAll();
    }

    public Profession add(String profession) {
        var entity = new Profession();
        entity.setName(profession);
        return professionRepository.save(entity);
    }

    public Profession delete(int id) {
        final var entity = professionRepository.findById(id).orElseThrow(ProfessionNotFoundException::new);
        professionRepository.delete(entity);
        return entity;
    }

}
