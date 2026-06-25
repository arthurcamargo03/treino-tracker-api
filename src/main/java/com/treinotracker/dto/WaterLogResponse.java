package com.treinotracker.dto;

import com.treinotracker.entity.WaterLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record WaterLogResponse(
        @Schema(description = "Id do registro de hidratação", example = "1") Long id,
        @Schema(description = "Data do registro") LocalDate date,
        @Schema(description = "Quantidade consumida no dia, em ml", example = "700") int consumedMl,
        @Schema(description = "Meta do dia, em ml", example = "3000") int goalMl,
        @Schema(description = "Percentual da meta já consumido", example = "23.33") double percent,
        @Schema(description = "Se a meta do dia já foi atingida", example = "false") boolean goalReached,
        @Schema(description = "Tamanho padrão da garrafa, em ml", example = "500") int bottleSizeMl,
        @Schema(description = "Quantidade de garrafas necessárias para bater a meta", example = "6") int bottlesForGoal,
        @Schema(description = "Quantidade de garrafas completas já registradas no dia", example = "3") int completedBottles,
        @Schema(description = "Quantidade de garrafas restantes para bater a meta", example = "3") int remainingBottles
) {
    public static WaterLogResponse from(
            WaterLog waterLog,
            int bottleSizeMl,
            int bottlesForGoal,
            int completedBottles,
            int remainingBottles
    ) {
        return new WaterLogResponse(
                waterLog.getId(),
                waterLog.getDate(),
                waterLog.getConsumedMl(),
                waterLog.getGoalMl(),
                waterLog.percent(),
                waterLog.goalReached(),
                bottleSizeMl,
                bottlesForGoal,
                completedBottles,
                remainingBottles
        );
    }
}
