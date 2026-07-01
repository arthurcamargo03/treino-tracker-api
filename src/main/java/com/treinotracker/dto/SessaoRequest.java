package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record SessaoRequest(
        @Schema(description = "Semana do ciclo de treino", example = "1") @Positive int semana,
        @Schema(description = "Séries realizadas na sessão, uma por posição")
        @NotEmpty @Valid List<SerieRequest> series
) {
}
