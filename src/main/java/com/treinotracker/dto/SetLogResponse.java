package com.treinotracker.dto;

import com.treinotracker.entity.SetLog;

import java.time.LocalDate;

public record SetLogResponse(
        Long id,
        Long exerciseId,
        String exerciseName,
        int week,
        double weight,
        int reps,
        int sets,
        LocalDate date,
        double volume,
        double estimated1RM
) {
    public static SetLogResponse from(SetLog setLog) {
        return new SetLogResponse(
                setLog.getId(),
                setLog.getExercise().getId(),
                setLog.getExercise().getName(),
                setLog.getWeek(),
                setLog.getWeight(),
                setLog.getReps(),
                setLog.getSets(),
                setLog.getDate(),
                setLog.volume(),
                setLog.estimated1RM()
        );
    }
}
