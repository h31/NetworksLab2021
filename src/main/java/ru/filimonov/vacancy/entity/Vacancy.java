package ru.filimonov.vacancy.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "vacancy")
@NoArgsConstructor
@Getter
@Setter
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "profession")
    private Profession profession;

    @Column(nullable = false)
    private String company, position;

    @Column(nullable = false)
    private Integer minAge, maxAge, salary;
}
