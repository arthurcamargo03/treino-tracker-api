package com.treinotracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "water_logs", uniqueConstraints = @UniqueConstraint(columnNames = "date"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WaterLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private LocalDate date;

    @PositiveOrZero
    @Column(name = "consumed_ml", nullable = false)
    private int consumedMl;

    @PositiveOrZero
    @Column(name = "goal_ml", nullable = false)
    private int goalMl;

    public WaterLog(LocalDate date, int consumedMl, int goalMl) {
        this.date = date;
        this.consumedMl = consumedMl;
        this.goalMl = goalMl;
    }

    public double percent() {
        return (consumedMl * 100.0) / goalMl;
    }

    public boolean goalReached() {
        return consumedMl >= goalMl;
    }
}
