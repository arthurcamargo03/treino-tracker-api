package com.treinotracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "sessoes_exercicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessaoExercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Positive
    @Column(nullable = false)
    private int semana;

    @NotNull
    @Column(nullable = false)
    private LocalDate data;

    public SessaoExercicio(Exercise exercise, int semana, LocalDate data) {
        this.exercise = exercise;
        this.semana = semana;
        this.data = data;
    }
}
