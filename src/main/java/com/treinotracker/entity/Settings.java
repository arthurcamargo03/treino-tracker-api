package com.treinotracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive
    @Column(name = "daily_goal_ml", nullable = false)
    private int dailyGoalMl;

    @Positive
    @Column(name = "bottle_size_ml", nullable = false)
    private int bottleSizeMl;

    public Settings(int dailyGoalMl, int bottleSizeMl) {
        this.dailyGoalMl = dailyGoalMl;
        this.bottleSizeMl = bottleSizeMl;
    }
}
