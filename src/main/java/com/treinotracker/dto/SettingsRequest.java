package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record SettingsRequest(
        @Schema(description = "Nova meta diária de consumo, em ml", example = "3000") @Positive int dailyGoalMl,
        @Schema(description = "Novo tamanho padrão da garrafa, em ml", example = "500") @Positive int bottleSizeMl
) {
}
