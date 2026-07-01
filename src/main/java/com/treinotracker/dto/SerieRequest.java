package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record SerieRequest(
        @Schema(description = "Posição da série dentro da sessão, começando em 1", example = "1")
        @Positive int posicao,
        @Schema(description = "Carga usada na série, em kg", example = "60.0") @Positive double carga,
        @Schema(description = "Repetições realizadas na série", example = "10") @Positive int reps
) {
}
