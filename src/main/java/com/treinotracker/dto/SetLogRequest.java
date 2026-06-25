package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record SetLogRequest(
        @Schema(description = "Semana do ciclo de treino", example = "1") @Positive int week,
        @Schema(description = "Carga utilizada, em kg", example = "60.0") @Positive double weight,
        @Schema(description = "Repetições realizadas na série", example = "10") @Positive int reps,
        @Schema(description = "Número de séries realizadas", example = "3") @Positive int sets
) {
}
