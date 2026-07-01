package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExerciseRequest(
        @Schema(description = "Nome do exercício", example = "Supino reto") @NotBlank String name,
        @Schema(description = "Grupo muscular trabalhado", example = "Peito") @NotBlank String muscleGroup,
        @Schema(description = "Id do treino ao qual o exercício pertence", example = "1") @NotNull Long trainingDayId,
        @Schema(description = "Quantas séries válidas o exercício tem (padrão 3 quando omitido)", example = "3")
        @Positive Integer seriesValidas
) {
}
