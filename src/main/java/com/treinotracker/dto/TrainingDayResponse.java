package com.treinotracker.dto;

import com.treinotracker.entity.TrainingDay;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

public record TrainingDayResponse(
        @Schema(description = "Id do treino", example = "1") Long id,
        @Schema(description = "Nome do treino", example = "Treino A — Peito") String name,
        @Schema(description = "Dia da semana", example = "MONDAY") DayOfWeek dayOfWeek,
        @Schema(description = "Nome do dia da semana em português", example = "segunda-feira") String dayOfWeekLabel
) {
    public static TrainingDayResponse from(TrainingDay trainingDay) {
        return new TrainingDayResponse(
                trainingDay.getId(),
                trainingDay.getName(),
                trainingDay.getDayOfWeek(),
                trainingDay.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
        );
    }
}
