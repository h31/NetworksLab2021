package ru.filimonov.vacancy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VacancyRequestDto {
    @NotNull
    private String profession, company, position;
    @Min(1)
    @NotNull
    private int minAge, maxAge, salary;
}
