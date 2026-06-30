package com.treinotracker.service;

import com.treinotracker.dto.PontoSerie;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class WorkoutService {

    private final ExerciseRepository exerciseRepository;
    private final SetLogRepository setLogRepository;
    private final TrainingDayRepository trainingDayRepository;
    private final SessaoExercicioRepository sessaoExercicioRepository;
    private final SerieRepository serieRepository;

    public WorkoutService(ExerciseRepository exerciseRepository, SetLogRepository setLogRepository,
                           TrainingDayRepository trainingDayRepository,
                           SessaoExercicioRepository sessaoExercicioRepository,
                           SerieRepository serieRepository) {
        this.exerciseRepository = exerciseRepository;
        this.setLogRepository = setLogRepository;
        this.trainingDayRepository = trainingDayRepository;
        this.sessaoExercicioRepository = sessaoExercicioRepository;
        this.serieRepository = serieRepository;
    }

    public List<Exercise> getExercises() {
        return exerciseRepository.findAllWithTrainingDay();
    }

    public Exercise getExercise(Long exerciseId) {
        return findExerciseWithTrainingDayOrThrow(exerciseId);
    }

    @Transactional
    public Exercise addExercise(String name, String group, Long trainingDayId) {
        if (exerciseRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new DuplicateResourceException("Exercício já existe: " + name);
        }
        TrainingDay trainingDay = findTrainingDayOrThrow(trainingDayId);
        return exerciseRepository.save(new Exercise(name, group, trainingDay));
    }

    public List<TrainingDay> getTrainingDays() {
        return trainingDayRepository.findAll().stream()
                .sorted(Comparator.comparingInt(day -> day.getDayOfWeek().getValue()))
                .toList();
    }

    @Transactional
    public TrainingDay addTrainingDay(String name, DayOfWeek dayOfWeek) {
        if (trainingDayRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new DuplicateResourceException("Treino já existe: " + name);
        }
        return trainingDayRepository.save(new TrainingDay(name, dayOfWeek));
    }

    @Transactional
    public SetLog logSet(Long exerciseId, int week, double weight, int reps, int sets) {
        validatePositive(week, "Semana");
        validatePositive(weight, "Carga");
        validatePositive(reps, "Repetições");
        validatePositive(sets, "Séries");
        Exercise exercise = findExerciseOrThrow(exerciseId);
        SetLog setLog = new SetLog(exercise, week, weight, reps, sets, LocalDate.now());
        return setLogRepository.save(setLog);
    }

    public List<WeekSummary> getProgression(Long exerciseId) {
        findExerciseOrThrow(exerciseId);

        TreeMap<Integer, SetLog> bestPerWeek = new TreeMap<>();
        for (SetLog set : setLogRepository.findByExerciseIdOrderByWeekAsc(exerciseId)) {
            SetLog current = bestPerWeek.get(set.getWeek());
            if (current == null || set.estimated1RM() > current.estimated1RM()) {
                bestPerWeek.put(set.getWeek(), set);
            }
        }

        List<WeekSummary> summaries = new ArrayList<>();
        Double previous1RM = null;
        for (Map.Entry<Integer, SetLog> entry : bestPerWeek.entrySet()) {
            SetLog best = entry.getValue();
            double oneRm = best.estimated1RM();
            Double trend = previous1RM == null ? null : ((oneRm - previous1RM) / previous1RM) * 100.0;
            summaries.add(new WeekSummary(
                    best.getWeek(),
                    best.getWeight(),
                    best.getReps(),
                    best.getSets(),
                    best.volume(),
                    oneRm,
                    trend
            ));
            previous1RM = oneRm;
        }
        return summaries;
    }

    @Transactional
    public SessaoExercicio registrarSessao(Long exerciseId, int semana, List<Serie> series) {
        validatePositive(semana, "Semana");
        if (series == null || series.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma série");
        }
        Exercise exercise = findExerciseOrThrow(exerciseId);
        SessaoExercicio sessao = sessaoExercicioRepository.save(new SessaoExercicio(exercise, semana, LocalDate.now()));
        for (Serie serie : series) {
            validatePositive(serie.getPosicao(), "Posição");
            validatePositive(serie.getCarga(), "Carga");
            validatePositive(serie.getReps(), "Repetições");
            serie.setSessaoExercicio(sessao);
            serieRepository.save(serie);
        }
        return sessao;
    }

    public List<ProgressaoPosicao> getProgressaoPorPosicao(Long exerciseId) {
        findExerciseOrThrow(exerciseId);

        // posição -> (semana -> melhor série da posição naquela semana, por 1RM)
        TreeMap<Integer, TreeMap<Integer, Serie>> bestPerPositionWeek = new TreeMap<>();
        TreeMap<Integer, Boolean> semanas = new TreeMap<>();
        for (Serie serie : serieRepository.findAllByExerciseId(exerciseId)) {
            int posicao = serie.getPosicao();
            int semana = serie.getSessaoExercicio().getSemana();
            semanas.put(semana, Boolean.TRUE);
            TreeMap<Integer, Serie> porSemana = bestPerPositionWeek.computeIfAbsent(posicao, key -> new TreeMap<>());
            Serie atual = porSemana.get(semana);
            if (atual == null || serie.estimated1RM() > atual.estimated1RM()) {
                porSemana.put(semana, serie);
            }
        }

        List<ProgressaoPosicao> resultado = new ArrayList<>();
        for (Map.Entry<Integer, TreeMap<Integer, Serie>> entry : bestPerPositionWeek.entrySet()) {
            TreeMap<Integer, Serie> porSemana = entry.getValue();
            List<PontoSerie> pontos = new ArrayList<>();
            for (Map.Entry<Integer, Serie> semanaEntry : porSemana.entrySet()) {
                int semana = semanaEntry.getKey();
                Serie serie = semanaEntry.getValue();
                Integer semanaAnterior = semanas.lowerKey(semana);
                // Só calcula tendência se a posição existir também na semana imediatamente anterior.
                Double trend = null;
                if (semanaAnterior != null && porSemana.containsKey(semanaAnterior)) {
                    double anterior1RM = porSemana.get(semanaAnterior).estimated1RM();
                    trend = ((serie.estimated1RM() - anterior1RM) / anterior1RM) * 100.0;
                }
                pontos.add(new PontoSerie(semana, serie.getCarga(), serie.getReps(), serie.estimated1RM(), trend));
            }
            resultado.add(new ProgressaoPosicao(entry.getKey(), pontos));
        }
        return resultado;
    }

    public boolean isProgressing(Long exerciseId) {
        List<WeekSummary> progression = getProgression(exerciseId);
        if (progression.isEmpty()) {
            return false;
        }
        WeekSummary last = progression.get(progression.size() - 1);
        return last.trendPercent() != null && last.trendPercent() > 0;
    }

    private Exercise findExerciseOrThrow(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercício não encontrado: " + exerciseId));
    }

    private Exercise findExerciseWithTrainingDayOrThrow(Long exerciseId) {
        return exerciseRepository.findByIdWithTrainingDay(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercício não encontrado: " + exerciseId));
    }

    private TrainingDay findTrainingDayOrThrow(Long trainingDayId) {
        return trainingDayRepository.findById(trainingDayId)
                .orElseThrow(() -> new ResourceNotFoundException("Treino não encontrado: " + trainingDayId));
    }

    private void validatePositive(double value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " deve ser positiva: " + value);
        }
    }
}
