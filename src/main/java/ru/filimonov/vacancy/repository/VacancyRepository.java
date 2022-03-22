package ru.filimonov.vacancy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.filimonov.vacancy.entity.Vacancy;

import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, Integer> {
    @Query("select v from Vacancy v where (:profession is null or v.profession.name = :profession) "
            + "and (:company is null or v.company = :company) and (:position is null or v.position = :position) "
            + "and (:salary is null or v.salary >= :salary) and (:minAge is null or v.minAge >= :minAge) "
            + "and (:maxAge is null or v.maxAge <= :maxAge)")
    public List<Vacancy> search(
            @Param("profession") String profession,
            @Param("company") String company,
            @Param("position") String position,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("salary") Integer salary
            );
}
