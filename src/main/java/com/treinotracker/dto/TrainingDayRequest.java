package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;

public record TrainingDayRequest(
        @Schema(description = "Nome do treino", example = "Treino A — Peito") @NotBlank String name,
        @Schema(description = "Dia da semana", example = "MONDAY") @NotNull DayOfWeek dayOfWeek
) {
}
