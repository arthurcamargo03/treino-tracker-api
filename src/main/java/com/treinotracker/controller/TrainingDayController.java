package com.treinotracker.controller;

import com.treinotracker.dto.ApiErrorResponse;
import com.treinotracker.dto.TrainingDayRequest;
import com.treinotracker.dto.TrainingDayResponse;
import com.treinotracker.entity.TrainingDay;
import com.treinotracker.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/training-days")
@Tag(name = "Treinos", description = "Dias de treino usados para organizar os exercícios (ex: Treino A — Peito, segunda-feira)")
public class TrainingDayController {

    private final WorkoutService workoutService;

    public TrainingDayController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    @Operation(summary = "Lista os treinos cadastrados", description = "Ordenados por dia da semana (segunda a domingo)")
    public ResponseEntity<List<TrainingDayResponse>> getAll() {
        List<TrainingDayResponse> trainingDays = workoutService.getTrainingDays().stream()
                .map(TrainingDayResponse::from)
                .toList();
        return ResponseEntity.ok(trainingDays);
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo treino", description = "Rejeita nomes duplicados (case-insensitive)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Treino criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe um treino com esse nome",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<TrainingDayResponse> create(@Valid @RequestBody TrainingDayRequest request) {
        TrainingDay trainingDay = workoutService.addTrainingDay(request.name(), request.dayOfWeek());
        return ResponseEntity.created(URI.create("/api/training-days/" + trainingDay.getId()))
                .body(TrainingDayResponse.from(trainingDay));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um treino",
            description = "Remove também, em cascata, todos os exercícios do treino e seus registros")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Treino excluído"),
            @ApiResponse(responseCode = "404", description = "Treino não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workoutService.deleteTrainingDay(id);
        return ResponseEntity.noContent().build();
    }
}
