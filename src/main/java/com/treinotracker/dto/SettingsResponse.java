package com.treinotracker.dto;

import com.treinotracker.entity.Settings;

public record SettingsResponse(
        int dailyGoalMl,
        int bottleSizeMl
) {
    public static SettingsResponse from(Settings settings) {
        return new SettingsResponse(settings.getDailyGoalMl(), settings.getBottleSizeMl());
    }
}
