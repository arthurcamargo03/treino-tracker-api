package com.treinotracker.dto;

public record WeekSummary(
        int week,
        double weight,
        int reps,
        int sets,
        double volume,
        double estimated1RM,
        Double trendPercent
) {
}
