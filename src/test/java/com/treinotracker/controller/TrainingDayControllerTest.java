package com.treinotracker.controller;

import com.treinotracker.entity.TrainingDay;
import com.treinotracker.exception.DuplicateResourceException;
import com.treinotracker.service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingDayController.class)
class TrainingDayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutService workoutService;

    @Test
    void getAll_returnsTrainingDays() throws Exception {
        TrainingDay trainingDay = trainingDayWithId(1L, "Treino A - Peito", DayOfWeek.MONDAY);
        when(workoutService.getTrainingDays()).thenReturn(List.of(trainingDay));

        mockMvc.perform(get("/api/training-days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[0].dayOfWeekLabel").value("segunda-feira"));
    }

    @Test
    void create_returns201_whenRequestIsValid() throws Exception {
        TrainingDay trainingDay = trainingDayWithId(1L, "Treino A - Peito", DayOfWeek.MONDAY);
        when(workoutService.addTrainingDay(eq("Treino A - Peito"), eq(DayOfWeek.MONDAY))).thenReturn(trainingDay);

        mockMvc.perform(post("/api/training-days")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Treino A - Peito\",\"dayOfWeek\":\"MONDAY\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/training-days/1"))
                .andExpect(jsonPath("$.name").value("Treino A - Peito"));
    }

    @Test
    void create_returns409_whenNameIsDuplicate() throws Exception {
        when(workoutService.addTrainingDay(eq("Treino A - Peito"), eq(DayOfWeek.MONDAY)))
                .thenThrow(new DuplicateResourceException("Treino já existe: Treino A - Peito"));

        mockMvc.perform(post("/api/training-days")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Treino A - Peito\",\"dayOfWeek\":\"MONDAY\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Treino já existe: Treino A - Peito"));
    }

    private static TrainingDay trainingDayWithId(Long id, String name, DayOfWeek dayOfWeek) {
        TrainingDay trainingDay = new TrainingDay(name, dayOfWeek);
        trainingDay.setId(id);
        return trainingDay;
    }
}
