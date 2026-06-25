package com.treinotracker.service;

import com.treinotracker.dto.WeekSummary;
import com.treinotracker.entity.Exercise;
import com.treinotracker.entity.SetLog;
import com.treinotracker.entity.TrainingDay;
import com.treinotracker.exception.DuplicateResourceException;
import com.treinotracker.exception.ResourceNotFoundException;
import com.treinotracker.repository.ExerciseRepository;
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

    public WorkoutService(ExerciseRepository exerciseRepository, SetLogRepository setLogRepository,
                           TrainingDayRepository trainingDayRepository) {
        this.exerciseRepository = exerciseRepository;
        this.setLogRepository = setLogRepository;
        this.trainingDayRepository = trainingDayRepository;
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
