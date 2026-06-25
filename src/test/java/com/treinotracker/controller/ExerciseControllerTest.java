package com.treinotracker.controller;

import com.treinotracker.entity.Exercise;
import com.treinotracker.exception.DuplicateResourceException;
import com.treinotracker.exception.ResourceNotFoundException;
import com.treinotracker.service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExerciseController.class)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutService workoutService;

    private static Exercise exerciseWithId(Long id, String name, String muscleGroup) {
        Exercise exercise = new Exercise(name, muscleGroup);
        exercise.setId(id);
        return exercise;
    }

    @Test
    void getAll_returnsExerciseList() throws Exception {
        when(workoutService.getExercises()).thenReturn(List.of(exerciseWithId(1L, "Supino reto", "Peito")));

        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Supino reto"))
                .andExpect(jsonPath("$[0].muscleGroup").value("Peito"));
    }

    @Test
    void create_returns201WithLocation_whenRequestIsValid() throws Exception {
        when(workoutService.addExercise("Supino reto", "Peito"))
                .thenReturn(exerciseWithId(1L, "Supino reto", "Peito"));

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Supino reto\",\"muscleGroup\":\"Peito\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/exercises/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Supino reto"));
    }

    @Test
    void create_returns400_whenFieldsAreBlank() throws Exception {
        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"muscleGroup\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.muscleGroup").exists());
    }

    @Test
    void create_returns409_whenNameIsDuplicate() throws Exception {
        when(workoutService.addExercise("Supino reto", "Peito"))
                .thenThrow(new DuplicateResourceException("Exercício já existe: Supino reto"));

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Supino reto\",\"muscleGroup\":\"Peito\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Exercício já existe: Supino reto"));
    }

    @Test
    void getById_returns200_whenExerciseExists() throws Exception {
        when(workoutService.getExercise(1L)).thenReturn(exerciseWithId(1L, "Supino reto", "Peito"));

        mockMvc.perform(get("/api/exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Supino reto"));
    }

    @Test
    void getById_returns404_whenExerciseMissing() throws Exception {
        when(workoutService.getExercise(999L))
                .thenThrow(new ResourceNotFoundException("Exercício não encontrado: 999"));

        mockMvc.perform(get("/api/exercises/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Exercício não encontrado: 999"));
    }
}
