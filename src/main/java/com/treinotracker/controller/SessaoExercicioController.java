package com.treinotracker.controller;

import com.treinotracker.dto.ApiErrorResponse;
import com.treinotracker.dto.ProgressaoPosicao;
import com.treinotracker.dto.SessaoRequest;
import com.treinotracker.dto.SessaoResponse;
import com.treinotracker.entity.Serie;
import com.treinotracker.entity.SessaoExercicio;
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
@Tag(name = "Sessões", description = "Registro de sessões com séries individuais e progressão por posição (1RM Epley)")
public class SessaoExercicioController {

    private final WorkoutService workoutService;

    public SessaoExercicioController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping("/sessoes")
    @Operation(summary = "Registra uma sessão com N séries individuais",
            description = "Salva a semana e cada série (posição, carga, reps) de uma vez")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sessão registrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exercício não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SessaoResponse> registrarSessao(
            @Parameter(description = "Id do exercício") @PathVariable Long id,
            @Valid @RequestBody SessaoRequest request) {
        List<Serie> series = request.series().stream()
                .map(s -> {
                    Serie serie = new Serie();
                    serie.setPosicao(s.posicao());
                    serie.setCarga(s.carga());
                    serie.setReps(s.reps());
                    return serie;
                })
                .toList();
        SessaoExercicio sessao = workoutService.registrarSessao(id, request.semana(), series);
        return ResponseEntity.created(URI.create("/api/exercises/" + id + "/sessoes/" + sessao.getId()))
                .body(SessaoResponse.from(sessao));
    }

    @GetMapping("/progressao-series")
    @Operation(summary = "Progressão do 1RM por posição de série",
            description = "Para cada posição (1, 2, 3...), a evolução semana a semana com a variação frente à semana "
                    + "anterior; a posição é marcada como primeira vez quando não existia na semana anterior")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progressão calculada"),
            @ApiResponse(responseCode = "404", description = "Exercício não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<ProgressaoPosicao>> progressaoPorPosicao(
            @Parameter(description = "Id do exercício") @PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getProgressaoPorPosicao(id));
    }
}
