package com.treinotracker.dto;

import jakarta.validation.constraints.NotBlank;

public record ExerciseRequest(
        @NotBlank String name,
        @NotBlank String muscleGroup
) {
}
