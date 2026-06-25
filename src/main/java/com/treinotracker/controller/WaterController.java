package com.treinotracker.controller;

import com.treinotracker.dto.ApiErrorResponse;
import com.treinotracker.dto.DrinkRequest;
import com.treinotracker.dto.SettingsRequest;
import com.treinotracker.dto.SettingsResponse;
import com.treinotracker.dto.WaterLogResponse;
import com.treinotracker.entity.Settings;
import com.treinotracker.entity.WaterLog;
import com.treinotracker.service.WaterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/water")
@Tag(name = "Hidratação", description = "Acompanhamento de consumo diário de água")
public class WaterController {

    private final WaterService waterService;

    public WaterController(WaterService waterService) {
        this.waterService = waterService;
    }

    @GetMapping("/settings")
    @Operation(summary = "Retorna a meta diária e o tamanho de garrafa configurados atualmente")
    public ResponseEntity<SettingsResponse> getSettings() {
        return ResponseEntity.ok(SettingsResponse.from(waterService.getSettings()));
    }

    @GetMapping("/today")
    @Operation(summary = "Retorna o registro de hidratação de hoje", description = "Cria o registro do dia com a meta atual caso ainda não exista")
    public ResponseEntity<WaterLogResponse> today() {
        return ResponseEntity.ok(WaterLogResponse.from(waterService.today()));
    }

    @PostMapping("/drink")
    @Operation(summary = "Registra consumo de água",
            description = "Sem corpo, ou com \"ml\" nulo, usa o tamanho de garrafa configurado; informe \"ml\" para registrar uma quantidade customizada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consumo registrado"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<WaterLogResponse> drink(@Valid @RequestBody(required = false) DrinkRequest request) {
        WaterLog log = (request == null || request.ml() == null)
                ? waterService.drinkBottle()
                : waterService.drink(request.ml());
        return ResponseEntity.ok(WaterLogResponse.from(log));
    }

    @PutMapping("/settings")
    @Operation(summary = "Atualiza a meta diária e o tamanho da garrafa", description = "Substitui as configurações atuais; ambos os campos são obrigatórios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configurações atualizadas"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SettingsResponse> updateSettings(@Valid @RequestBody SettingsRequest request) {
        Settings settings = waterService.updateSettings(request.dailyGoalMl(), request.bottleSizeMl());
        return ResponseEntity.ok(SettingsResponse.from(settings));
    }
}
