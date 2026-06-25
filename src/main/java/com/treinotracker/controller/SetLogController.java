package com.treinotracker.controller;

import com.treinotracker.dto.ApiErrorResponse;
import com.treinotracker.dto.SetLogRequest;
import com.treinotracker.dto.SetLogResponse;
import com.treinotracker.dto.WeekSummary;
import com.treinotracker.entity.SetLog;
import com.treinotracker.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/exercises/{id}")
@Tag(name = "Séries", description = "Registro de séries de treino e progressão semanal (1RM estimado pela fórmula de Epley)")
public class SetLogController {

    private final WorkoutService workoutService;

    public SetLogController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping("/sets")
    @Operation(summary = "Registra uma série de treino", description = "Calcula volume e 1RM estimado (Epley) automaticamente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Série registrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exercício não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SetLogResponse> logSet(
            @Parameter(description = "Id do exercício") @PathVariable Long id,
            @Valid @RequestBody SetLogRequest request) {
        SetLog setLog = workoutService.logSet(id, request.week(), request.weight(), request.reps(), request.sets());
        return ResponseEntity.created(URI.create("/api/exercises/" + id + "/sets/" + setLog.getId()))
                .body(SetLogResponse.from(setLog));
    }

    @GetMapping("/progression")
    @Operation(summary = "Retorna a progressão semanal do exercício",
            description = "Melhor série de cada semana (maior 1RM), com a variação percentual do 1RM em relação à semana anterior (null na primeira semana)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progressão calculada"),
            @ApiResponse(responseCode = "404", description = "Exercício não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<WeekSummary>> progression(
            @Parameter(description = "Id do exercício") @PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getProgression(id));
    }
}
