package ru.filimonov.vacancy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfessionRequestDto {

    private Integer id;
    private String name;
}
