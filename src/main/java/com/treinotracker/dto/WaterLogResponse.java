package com.treinotracker.dto;

import com.treinotracker.entity.WaterLog;

import java.time.LocalDate;

public record WaterLogResponse(
        Long id,
        LocalDate date,
        int consumedMl,
        int goalMl,
        double percent,
        boolean goalReached
) {
    public static WaterLogResponse from(WaterLog waterLog) {
        return new WaterLogResponse(
                waterLog.getId(),
                waterLog.getDate(),
                waterLog.getConsumedMl(),
                waterLog.getGoalMl(),
                waterLog.percent(),
                waterLog.goalReached()
        );
    }
}
