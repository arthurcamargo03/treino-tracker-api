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
@Table(name = "set_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Positive
    @Column(nullable = false)
    private int week;

    @Positive
    @Column(nullable = false)
    private double weight;

    @Positive
    @Column(nullable = false)
    private int reps;

    @Positive
    @Column(nullable = false)
    private int sets;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    public SetLog(Exercise exercise, int week, double weight, int reps, int sets, LocalDate date) {
        this.exercise = exercise;
        this.week = week;
        this.weight = weight;
        this.reps = reps;
        this.sets = sets;
        this.date = date;
    }

    public double volume() {
        return sets * reps * weight;
    }

    public double estimated1RM() {
        return weight * (1 + reps / 30.0);
    }
}
