package com.treinotracker.controller;

import com.treinotracker.dto.PontoSerie;
import com.treinotracker.dto.ProgressaoPosicao;
import com.treinotracker.entity.Exercise;
import com.treinotracker.entity.Serie;
import com.treinotracker.entity.SessaoExercicio;
import com.treinotracker.entity.TrainingDay;
import com.treinotracker.exception.ResourceNotFoundException;
import com.treinotracker.service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessaoExercicioController.class)
class SessaoExercicioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutService workoutService;

    private Exercise exercise(Long id) {
        TrainingDay trainingDay = new TrainingDay("Treino A - Peito", DayOfWeek.MONDAY);
        trainingDay.setId(1L);
        Exercise exercise = new Exercise("Supino reto", "Peito", trainingDay);
        exercise.setId(id);
        return exercise;
    }

    @Test
    @SuppressWarnings("unchecked")
    void registrarSessao_returns201_andForwardsSeries() throws Exception {
        Exercise exercise = exercise(10L);
        SessaoExercicio sessao = new SessaoExercicio(exercise, 1, LocalDate.of(2026, 6, 25));
        sessao.setId(7L);
        when(workoutService.registrarSessao(eq(10L), eq(1), any())).thenReturn(sessao);

        mockMvc.perform(post("/api/exercises/10/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semana\":1,\"series\":["
                                + "{\"posicao\":1,\"carga\":60.0,\"reps\":10},"
                                + "{\"posicao\":2,\"carga\":57.5,\"reps\":10}]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/exercises/10/sessoes/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.exerciseId").value(10))
                .andExpect(jsonPath("$.semana").value(1));

        ArgumentCaptor<List<Serie>> captor = ArgumentCaptor.forClass(List.class);
        verify(workoutService).registrarSessao(eq(10L), eq(1), captor.capture());
        List<Serie> series = captor.getValue();
        assertThat(series).extracting(Serie::getPosicao).containsExactly(1, 2);
        assertThat(series).extracting(Serie::getCarga).containsExactly(60.0, 57.5);
        assertThat(series).extracting(Serie::getReps).containsExactly(10, 10);
    }

    @Test
    void registrarSessao_returns400_whenSeriesEmpty() throws Exception {
        mockMvc.perform(post("/api/exercises/10/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semana\":1,\"series\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void progressaoPorPosicao_returnsPositions() throws Exception {
        when(workoutService.getProgressaoPorPosicao(10L))
                .thenReturn(List.of(new ProgressaoPosicao(1,
                        List.of(new PontoSerie(1, 60.0, 10, 80.0, null)))));

        mockMvc.perform(get("/api/exercises/10/progressao-series"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].posicao").value(1))
                .andExpect(jsonPath("$[0].pontos[0].estimated1RM").value(80.0))
                .andExpect(jsonPath("$[0].pontos[0].trendPercent").doesNotExist());
    }

    @Test
    void progressaoPorPosicao_returns404_whenExerciseMissing() throws Exception {
        when(workoutService.getProgressaoPorPosicao(999L))
                .thenThrow(new ResourceNotFoundException("Exercício não encontrado: 999"));

        mockMvc.perform(get("/api/exercises/999/progressao-series"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Exercício não encontrado: 999"));
    }
}
