package com.treinotracker.service;

import com.treinotracker.dto.ProgressaoPosicao;
import com.treinotracker.dto.WeekSummary;
import com.treinotracker.entity.Exercise;
import com.treinotracker.entity.Serie;
import com.treinotracker.entity.SessaoExercicio;
import com.treinotracker.entity.SetLog;
import com.treinotracker.entity.TrainingDay;
import com.treinotracker.exception.DuplicateResourceException;
import com.treinotracker.exception.ResourceNotFoundException;
import com.treinotracker.repository.ExerciseRepository;
import com.treinotracker.repository.SerieRepository;
import com.treinotracker.repository.SessaoExercicioRepository;
import com.treinotracker.repository.SetLogRepository;
import com.treinotracker.repository.TrainingDayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private SetLogRepository setLogRepository;

    @Mock
    private TrainingDayRepository trainingDayRepository;

    @Mock
    private SessaoExercicioRepository sessaoExercicioRepository;

    @Mock
    private SerieRepository serieRepository;

    private WorkoutService workoutService;

    @BeforeEach
    void setUp() {
        workoutService = new WorkoutService(exerciseRepository, setLogRepository, trainingDayRepository,
                sessaoExercicioRepository, serieRepository);
    }

    private static Exercise exerciseWithId(Long id, String name, String muscleGroup) {
        Exercise exercise = new Exercise(name, muscleGroup);
        exercise.setId(id);
        return exercise;
    }

    private static TrainingDay trainingDayWithId(Long id, String name, DayOfWeek dayOfWeek) {
        TrainingDay trainingDay = new TrainingDay(name, dayOfWeek);
        trainingDay.setId(id);
        return trainingDay;
    }

    @Test
    void addExercise_savesNewExercise_whenNameNotDuplicate() {
        TrainingDay trainingDay = trainingDayWithId(1L, "Treino A — Peito", DayOfWeek.MONDAY);
        when(exerciseRepository.findByNameIgnoreCase("Supino reto")).thenReturn(Optional.empty());
        when(trainingDayRepository.findById(1L)).thenReturn(Optional.of(trainingDay));
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Exercise result = workoutService.addExercise("Supino reto", "Peito", 1L);

        assertThat(result.getName()).isEqualTo("Supino reto");
        assertThat(result.getMuscleGroup()).isEqualTo("Peito");
        assertThat(result.getTrainingDay()).isEqualTo(trainingDay);
        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    void addExercise_throwsDuplicateResourceException_whenNameAlreadyExists() {
        when(exerciseRepository.findByNameIgnoreCase("Supino reto"))
                .thenReturn(Optional.of(exerciseWithId(1L, "Supino reto", "Peito")));

        assertThatThrownBy(() -> workoutService.addExercise("Supino reto", "Peito", 1L))
                .isInstanceOf(DuplicateResourceException.class);

        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void addExercise_throwsResourceNotFoundException_whenTrainingDayMissing() {
        when(exerciseRepository.findByNameIgnoreCase("Supino reto")).thenReturn(Optional.empty());
        when(trainingDayRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.addExercise("Supino reto", "Peito", 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void addTrainingDay_savesNewTrainingDay_whenNameNotDuplicate() {
        when(trainingDayRepository.findByNameIgnoreCase("Treino A — Peito")).thenReturn(Optional.empty());
        when(trainingDayRepository.save(any(TrainingDay.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrainingDay result = workoutService.addTrainingDay("Treino A — Peito", DayOfWeek.MONDAY);

        assertThat(result.getName()).isEqualTo("Treino A — Peito");
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void addTrainingDay_throwsDuplicateResourceException_whenNameAlreadyExists() {
        when(trainingDayRepository.findByNameIgnoreCase("Treino A — Peito"))
                .thenReturn(Optional.of(trainingDayWithId(1L, "Treino A — Peito", DayOfWeek.MONDAY)));

        assertThatThrownBy(() -> workoutService.addTrainingDay("Treino A — Peito", DayOfWeek.MONDAY))
                .isInstanceOf(DuplicateResourceException.class);

        verify(trainingDayRepository, never()).save(any());
    }

    @Test
    void logSet_throwsResourceNotFoundException_whenExerciseMissing() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.logSet(99L, 1, 60.0, 10, 3))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void logSet_savesSetLogForExistingExercise() {
        Exercise exercise = exerciseWithId(1L, "Supino reto", "Peito");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(setLogRepository.save(any(SetLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SetLog result = workoutService.logSet(1L, 1, 60.0, 10, 3);

        assertThat(result.getExercise()).isEqualTo(exercise);
        assertThat(result.getWeek()).isEqualTo(1);
        assertThat(result.getWeight()).isEqualTo(60.0);
        verify(setLogRepository).save(any(SetLog.class));
    }

    @Test
    void getProgression_calculatesEpley1RM_andKeepsBestSetPerWeek() {
        Exercise exercise = exerciseWithId(1L, "Supino reto", "Peito");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        SetLog week1 = new SetLog(exercise, 1, 100.0, 10, 3, LocalDate.now());
        SetLog week2Worse = new SetLog(exercise, 2, 100.0, 5, 3, LocalDate.now());
        SetLog week2Better = new SetLog(exercise, 2, 110.0, 8, 3, LocalDate.now());
        when(setLogRepository.findByExerciseIdOrderByWeekAsc(1L))
                .thenReturn(List.of(week1, week2Worse, week2Better));

        List<WeekSummary> progression = workoutService.getProgression(1L);

        assertThat(progression).hasSize(2);

        double expectedWeek1Rm = 100.0 * (1 + 10 / 30.0);
        assertThat(progression.get(0).estimated1RM()).isCloseTo(expectedWeek1Rm, within(0.0001));
        assertThat(progression.get(0).trendPercent()).isNull();

        double expectedWeek2Rm = 110.0 * (1 + 8 / 30.0);
        assertThat(progression.get(1).weight()).isEqualTo(110.0);
        assertThat(progression.get(1).estimated1RM()).isCloseTo(expectedWeek2Rm, within(0.0001));
    }

    @Test
    void getProgression_calculatesTrendPercentBetweenWeeks() {
        Exercise exercise = exerciseWithId(1L, "Supino reto", "Peito");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        SetLog week1 = new SetLog(exercise, 1, 100.0, 10, 3, LocalDate.now());
        SetLog week2 = new SetLog(exercise, 2, 110.0, 10, 3, LocalDate.now());
        when(setLogRepository.findByExerciseIdOrderByWeekAsc(1L)).thenReturn(List.of(week1, week2));

        List<WeekSummary> progression = workoutService.getProgression(1L);

        double week1Rm = progression.get(0).estimated1RM();
        double week2Rm = progression.get(1).estimated1RM();
        double expectedTrend = ((week2Rm - week1Rm) / week1Rm) * 100.0;

        assertThat(progression.get(1).trendPercent()).isCloseTo(expectedTrend, within(0.0001));
    }

    @Test
    void getProgression_throwsResourceNotFoundException_whenExerciseMissing() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.getProgression(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void isProgressing_trueWhenLastWeekTrendIsPositive() {
        Exercise exercise = exerciseWithId(1L, "Supino reto", "Peito");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        SetLog week1 = new SetLog(exercise, 1, 100.0, 10, 3, LocalDate.now());
        SetLog week2 = new SetLog(exercise, 2, 110.0, 10, 3, LocalDate.now());
        when(setLogRepository.findByExerciseIdOrderByWeekAsc(1L)).thenReturn(List.of(week1, week2));

        assertThat(workoutService.isProgressing(1L)).isTrue();
    }

    @Test
    void isProgressing_falseWhenTrendIsFlatOrNegative() {
        Exercise exercise = exerciseWithId(1L, "Rosca direta", "Bíceps");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        SetLog week1 = new SetLog(exercise, 1, 20.0, 12, 3, LocalDate.now());
        SetLog week2 = new SetLog(exercise, 2, 20.0, 12, 3, LocalDate.now());
        when(setLogRepository.findByExerciseIdOrderByWeekAsc(1L)).thenReturn(List.of(week1, week2));

        assertThat(workoutService.isProgressing(1L)).isFalse();
    }

    @Test
    void isProgressing_falseWhenNoSetsLogged() {
        Exercise exercise = exerciseWithId(1L, "Supino reto", "Peito");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(setLogRepository.findByExerciseIdOrderByWeekAsc(1L)).thenReturn(List.of());

        assertThat(workoutService.isProgressing(1L)).isFalse();
    }

    @Test
    void registrarSessao_savesSessionAndAllSeries() {
        Exercise exercise = exerciseWithId(1L, "Supino reto", "Peito");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(sessaoExercicioRepository.save(any(SessaoExercicio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(serieRepository.save(any(Serie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Serie s1 = new Serie(null, 1, 60.0, 10);
        Serie s2 = new Serie(null, 2, 60.0, 9);
        Serie s3 = new Serie(null, 3, 60.0, 8);

        SessaoExercicio sessao = workoutService.registrarSessao(1L, 4, List.of(s1, s2, s3));

        assertThat(sessao.getExercise()).isEqualTo(exercise);
        assertThat(sessao.getSemana()).isEqualTo(4);
        assertThat(s1.getSessaoExercicio()).isEqualTo(sessao);
        assertThat(s3.getSessaoExercicio()).isEqualTo(sessao);
        verify(sessaoExercicioRepository).save(any(SessaoExercicio.class));
        verify(serieRepository, times(3)).save(any(Serie.class));
    }

    @Test
    void registrarSessao_throwsResourceNotFoundException_whenExerciseMissing() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.registrarSessao(99L, 1, List.of(new Serie(null, 1, 60.0, 10))))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(serieRepository, never()).save(any());
    }

    @Test
    void registrarSessao_throwsIllegalArgument_whenNoSeries() {
        assertThatThrownBy(() -> workoutService.registrarSessao(1L, 1, List.of()))
                .isInstanceOf(IllegalArgumentException.class);

        verify(sessaoExercicioRepository, never()).save(any());
        verify(serieRepository, never()).save(any());
    }

    @Test
    void getProgressaoPorPosicao_buildsEvolutionPerPosition_withTrend() {
        Exercise exercise = exerciseWithId(1L, "Supino reto", "Peito");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(serieRepository.findAllByExerciseId(1L)).thenReturn(List.of(
                serie(exercise, 1, 1, 100.0, 10),
                serie(exercise, 1, 2, 80.0, 10),
                serie(exercise, 2, 1, 110.0, 10),
                serie(exercise, 2, 2, 88.0, 10)
        ));

        List<ProgressaoPosicao> progressao = workoutService.getProgressaoPorPosicao(1L);

        assertThat(progressao).extracting(ProgressaoPosicao::posicao).containsExactly(1, 2);

        ProgressaoPosicao pos1 = posicao(progressao, 1);
        assertThat(pos1.pontos()).hasSize(2);
        assertThat(pos1.pontos().get(0).trendPercent()).isNull();
        // (110 - 100) / 100 * 100 = 10% (o fator de reps se cancela)
        assertThat(pos1.pontos().get(1).trendPercent()).isCloseTo(10.0, within(0.0001));

        ProgressaoPosicao pos2 = posicao(progressao, 2);
        assertThat(pos2.pontos().get(1).trendPercent()).isCloseTo(10.0, within(0.0001));
    }

    @Test
    void getProgressaoPorPosicao_marksPrimeiraVez_whenPositionAbsentInPreviousWeek() {
        Exercise exercise = exerciseWithId(1L, "Supino reto", "Peito");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        // Semana 2 tem menos séries (sem posição 3); semana 3 traz a posição 3 de volta.
        when(serieRepository.findAllByExerciseId(1L)).thenReturn(List.of(
                serie(exercise, 1, 1, 100.0, 10),
                serie(exercise, 1, 2, 80.0, 10),
                serie(exercise, 1, 3, 60.0, 10),
                serie(exercise, 2, 1, 110.0, 10),
                serie(exercise, 2, 2, 85.0, 10),
                serie(exercise, 3, 1, 115.0, 10),
                serie(exercise, 3, 2, 88.0, 10),
                serie(exercise, 3, 3, 70.0, 10)
        ));

        List<ProgressaoPosicao> progressao = workoutService.getProgressaoPorPosicao(1L);

        ProgressaoPosicao pos1 = posicao(progressao, 1);
        assertThat(pos1.pontos()).hasSize(3);
        assertThat(pos1.pontos().get(2).semana()).isEqualTo(3);
        // semana 3 vs semana 2: (115 - 110) / 110 * 100
        assertThat(pos1.pontos().get(2).trendPercent()).isCloseTo((5.0 / 110.0) * 100.0, within(0.0001));

        ProgressaoPosicao pos3 = posicao(progressao, 3);
        assertThat(pos3.pontos()).hasSize(2);
        // Posição 3 não existe na semana anterior (2) → "primeira vez", sem tendência. Nunca divide por zero.
        assertThat(pos3.pontos()).allSatisfy(ponto -> assertThat(ponto.trendPercent()).isNull());
    }

    @Test
    void getProgressaoPorPosicao_keepsBestSeriePerPositionWeek() {
        Exercise exercise = exerciseWithId(1L, "Supino reto", "Peito");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        // Mesma posição e semana, duas séries: fica a de maior 1RM (120x8 > 100x10).
        when(serieRepository.findAllByExerciseId(1L)).thenReturn(List.of(
                serie(exercise, 1, 1, 100.0, 10),
                serie(exercise, 1, 1, 120.0, 8)
        ));

        List<ProgressaoPosicao> progressao = workoutService.getProgressaoPorPosicao(1L);

        ProgressaoPosicao pos1 = posicao(progressao, 1);
        assertThat(pos1.pontos()).hasSize(1);
        assertThat(pos1.pontos().get(0).carga()).isEqualTo(120.0);
        assertThat(pos1.pontos().get(0).reps()).isEqualTo(8);
    }

    @Test
    void getProgressaoPorPosicao_throwsResourceNotFoundException_whenExerciseMissing() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.getProgressaoPorPosicao(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteExercise_removesSeriesSessionsSetLogsAndExercise() {
        Exercise exercise = exerciseWithId(1L, "Supino reto", "Peito");
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        workoutService.deleteExercise(1L);

        verify(serieRepository).deleteByExerciseId(1L);
        verify(sessaoExercicioRepository).deleteByExerciseId(1L);
        verify(setLogRepository).deleteByExerciseId(1L);
        verify(exerciseRepository).delete(exercise);
    }

    @Test
    void deleteExercise_throwsResourceNotFoundException_whenExerciseMissing() {
        when(exerciseRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.deleteExercise(9L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(exerciseRepository, never()).delete(any());
        verify(serieRepository, never()).deleteByExerciseId(any());
    }

    @Test
    void deleteTrainingDay_cascadesToExercisesAndTheirData() {
        TrainingDay trainingDay = trainingDayWithId(1L, "Treino A", DayOfWeek.MONDAY);
        Exercise supino = exerciseWithId(10L, "Supino reto", "Peito");
        Exercise crucifixo = exerciseWithId(11L, "Crucifixo", "Peito");
        when(trainingDayRepository.findById(1L)).thenReturn(Optional.of(trainingDay));
        when(exerciseRepository.findByTrainingDayId(1L)).thenReturn(List.of(supino, crucifixo));

        workoutService.deleteTrainingDay(1L);

        verify(serieRepository).deleteByExerciseId(10L);
        verify(serieRepository).deleteByExerciseId(11L);
        verify(exerciseRepository).delete(supino);
        verify(exerciseRepository).delete(crucifixo);
        verify(trainingDayRepository).delete(trainingDay);
    }

    @Test
    void deleteTrainingDay_throwsResourceNotFoundException_whenMissing() {
        when(trainingDayRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.deleteTrainingDay(9L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(trainingDayRepository, never()).delete(any());
    }

    private static Serie serie(Exercise exercise, int semana, int posicao, double carga, int reps) {
        SessaoExercicio sessao = new SessaoExercicio(exercise, semana, LocalDate.now());
        return new Serie(sessao, posicao, carga, reps);
    }

    private static ProgressaoPosicao posicao(List<ProgressaoPosicao> progressao, int posicao) {
        return progressao.stream()
                .filter(item -> item.posicao() == posicao)
                .findFirst()
                .orElseThrow();
    }
}
