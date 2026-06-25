package com.treinotracker.dto;

import com.treinotracker.entity.Exercise;

public record ExerciseResponse(
        Long id,
        String name,
        String muscleGroup
) {
    public static ExerciseResponse from(Exercise exercise) {
        return new ExerciseResponse(exercise.getId(), exercise.getName(), exercise.getMuscleGroup());
    }
}
