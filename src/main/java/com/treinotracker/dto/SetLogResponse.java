package com.treinotracker.dto;

import com.treinotracker.entity.SetLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record SetLogResponse(
        @Schema(description = "Id da série registrada", example = "1") Long id,
        @Schema(description = "Id do exercício", example = "1") Long exerciseId,
        @Schema(description = "Nome do exercício", example = "Supino reto") String exerciseName,
        @Schema(description = "Semana do ciclo de treino", example = "1") int week,
        @Schema(description = "Carga utilizada, em kg", example = "60.0") double weight,
        @Schema(description = "Repetições realizadas na série", example = "10") int reps,
        @Schema(description = "Número de séries realizadas", example = "3") int sets,
        @Schema(description = "Data em que a série foi registrada") LocalDate date,
        @Schema(description = "Volume total da série (sets × reps × weight)", example = "1800.0") double volume,
        @Schema(description = "1RM estimado pela fórmula de Epley", example = "80.0") double estimated1RM
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
