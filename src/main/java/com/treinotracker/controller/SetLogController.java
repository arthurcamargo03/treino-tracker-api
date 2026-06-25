package com.treinotracker.controller;

import com.treinotracker.dto.SetLogRequest;
import com.treinotracker.dto.SetLogResponse;
import com.treinotracker.dto.WeekSummary;
import com.treinotracker.entity.SetLog;
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
@RequestMapping("/api/exercises/{id}")
public class SetLogController {

    private final WorkoutService workoutService;

    public SetLogController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping("/sets")
    public ResponseEntity<SetLogResponse> logSet(@PathVariable Long id, @Valid @RequestBody SetLogRequest request) {
        SetLog setLog = workoutService.logSet(id, request.week(), request.weight(), request.reps(), request.sets());
        return ResponseEntity.created(URI.create("/api/exercises/" + id + "/sets/" + setLog.getId()))
                .body(SetLogResponse.from(setLog));
    }

    @GetMapping("/progression")
    public ResponseEntity<List<WeekSummary>> progression(@PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getProgression(id));
    }
}
