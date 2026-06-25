package com.treinotracker.controller;

import com.treinotracker.dto.DrinkRequest;
import com.treinotracker.dto.SettingsRequest;
import com.treinotracker.dto.SettingsResponse;
import com.treinotracker.dto.WaterLogResponse;
import com.treinotracker.entity.Settings;
import com.treinotracker.entity.WaterLog;
import com.treinotracker.service.WaterService;
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
public class WaterController {

    private final WaterService waterService;

    public WaterController(WaterService waterService) {
        this.waterService = waterService;
    }

    @GetMapping("/today")
    public ResponseEntity<WaterLogResponse> today() {
        return ResponseEntity.ok(WaterLogResponse.from(waterService.today()));
    }

    @PostMapping("/drink")
    public ResponseEntity<WaterLogResponse> drink(@Valid @RequestBody(required = false) DrinkRequest request) {
        WaterLog log = (request == null || request.ml() == null)
                ? waterService.drinkBottle()
                : waterService.drink(request.ml());
        return ResponseEntity.ok(WaterLogResponse.from(log));
    }

    @PutMapping("/settings")
    public ResponseEntity<SettingsResponse> updateSettings(@Valid @RequestBody SettingsRequest request) {
        waterService.updateDailyGoal(request.dailyGoalMl());
        Settings settings = waterService.updateBottleSize(request.bottleSizeMl());
        return ResponseEntity.ok(SettingsResponse.from(settings));
    }
}
