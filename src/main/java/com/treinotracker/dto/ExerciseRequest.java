package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ExerciseRequest(
        @Schema(description = "Nome do exercício", example = "Supino reto") @NotBlank String name,
        @Schema(description = "Grupo muscular trabalhado", example = "Peito") @NotBlank String muscleGroup
) {
}
