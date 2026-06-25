package com.treinotracker.controller;

import com.treinotracker.dto.ApiErrorResponse;
import com.treinotracker.dto.ExerciseRequest;
import com.treinotracker.dto.ExerciseResponse;
import com.treinotracker.entity.Exercise;
import com.treinotracker.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Exercícios", description = "Cadastro e consulta de exercícios")
public class ExerciseController {

    private final WorkoutService workoutService;

    public ExerciseController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os exercícios cadastrados")
    public ResponseEntity<List<ExerciseResponse>> getAll() {
        List<ExerciseResponse> exercises = workoutService.getExercises().stream()
                .map(ExerciseResponse::from)
                .toList();
        return ResponseEntity.ok(exercises);
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo exercício", description = "Rejeita nomes duplicados (case-insensitive)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Exercício criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe um exercício com esse nome",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ExerciseResponse> create(@Valid @RequestBody ExerciseRequest request) {
        Exercise exercise = workoutService.addExercise(request.name(), request.muscleGroup());
        return ResponseEntity.created(URI.create("/api/exercises/" + exercise.getId()))
                .body(ExerciseResponse.from(exercise));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um exercício pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercício encontrado"),
            @ApiResponse(responseCode = "404", description = "Exercício não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ExerciseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ExerciseResponse.from(workoutService.getExercise(id)));
    }
}
