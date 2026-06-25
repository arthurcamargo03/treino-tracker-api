package com.treinotracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SetLogRequest(
        @NotNull Long exerciseId,
        @Positive int week,
        @Positive double weight,
        @Positive int reps,
        @Positive int sets
) {
}
