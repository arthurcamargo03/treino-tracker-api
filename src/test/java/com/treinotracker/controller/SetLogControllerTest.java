package com.treinotracker.controller;

import com.treinotracker.dto.WeekSummary;
import com.treinotracker.entity.Exercise;
import com.treinotracker.entity.SetLog;
import com.treinotracker.entity.TrainingDay;
import com.treinotracker.exception.ResourceNotFoundException;
import com.treinotracker.service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SetLogController.class)
class SetLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutService workoutService;

    @Test
    void logSet_returns201_whenExerciseExists() throws Exception {
        TrainingDay trainingDay = new TrainingDay("Treino A - Peito", DayOfWeek.MONDAY);
        trainingDay.setId(1L);
        Exercise exercise = new Exercise("Supino reto", "Peito", trainingDay);
        exercise.setId(10L);
        SetLog setLog = new SetLog(exercise, 1, 60.0, 10, 3, LocalDate.of(2026, 6, 25));
        setLog.setId(5L);

        when(workoutService.logSet(eq(10L), eq(1), eq(60.0), eq(10), eq(3))).thenReturn(setLog);

        mockMvc.perform(post("/api/exercises/10/sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"week\":1,\"weight\":60.0,\"reps\":10,\"sets\":3}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/exercises/10/sets/5"))
                .andExpect(jsonPath("$.exerciseId").value(10))
                .andExpect(jsonPath("$.estimated1RM").value(80.0));
    }

    @Test
    void progression_returnsWeeklySummaries() throws Exception {
        when(workoutService.getProgression(10L))
                .thenReturn(List.of(new WeekSummary(1, 60.0, 10, 3, 1800.0, 80.0, null)));

        mockMvc.perform(get("/api/exercises/10/progression"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].week").value(1))
                .andExpect(jsonPath("$[0].estimated1RM").value(80.0));
    }

    @Test
    void progression_returns404_whenExerciseMissing() throws Exception {
        when(workoutService.getProgression(999L))
                .thenThrow(new ResourceNotFoundException("Exercício não encontrado: 999"));

        mockMvc.perform(get("/api/exercises/999/progression"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Exercício não encontrado: 999"));
    }
}
