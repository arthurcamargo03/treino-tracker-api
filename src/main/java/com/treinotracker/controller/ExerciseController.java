package com.treinotracker.controller;

import com.treinotracker.dto.ExerciseRequest;
import com.treinotracker.dto.ExerciseResponse;
import com.treinotracker.entity.Exercise;
import com.treinotracker.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final WorkoutService workoutService;

    public ExerciseController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> getAll() {
        List<ExerciseResponse> exercises = workoutService.getExercises().stream()
                .map(ExerciseResponse::from)
                .toList();
        return ResponseEntity.ok(exercises);
    }

    @PostMapping
    public ResponseEntity<ExerciseResponse> create(@Valid @RequestBody ExerciseRequest request) {
        Exercise exercise = workoutService.addExercise(request.name(), request.muscleGroup());
        return ResponseEntity.created(URI.create("/api/exercises/" + exercise.getId()))
                .body(ExerciseResponse.from(exercise));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ExerciseResponse.from(workoutService.getExercise(id)));
    }
}
