package com.treinotracker.dto;

import com.treinotracker.entity.Exercise;
import io.swagger.v3.oas.annotations.media.Schema;

public record ExerciseResponse(
        @Schema(description = "Id do exercício", example = "1") Long id,
        @Schema(description = "Nome do exercício", example = "Supino reto") String name,
        @Schema(description = "Grupo muscular trabalhado", example = "Peito") String muscleGroup,
        @Schema(description = "Treino ao qual o exercício pertence") TrainingDayResponse trainingDay
) {
    public static ExerciseResponse from(Exercise exercise) {
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getMuscleGroup(),
                TrainingDayResponse.from(exercise.getTrainingDay())
        );
    }
}
