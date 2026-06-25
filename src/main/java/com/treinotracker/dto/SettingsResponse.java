package com.treinotracker.dto;

import com.treinotracker.entity.Settings;
import io.swagger.v3.oas.annotations.media.Schema;

public record SettingsResponse(
        @Schema(description = "Meta diária de consumo, em ml", example = "3000") int dailyGoalMl,
        @Schema(description = "Tamanho padrão da garrafa, em ml", example = "500") int bottleSizeMl
) {
    public static SettingsResponse from(Settings settings) {
        return new SettingsResponse(settings.getDailyGoalMl(), settings.getBottleSizeMl());
    }
}
