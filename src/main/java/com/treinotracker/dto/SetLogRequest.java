package com.treinotracker.dto;

import jakarta.validation.constraints.Positive;

public record SetLogRequest(
        @Positive int week,
        @Positive double weight,
        @Positive int reps,
        @Positive int sets
) {
}
