package com.treinotracker.service;

import com.treinotracker.entity.Exercise;
import com.treinotracker.entity.SetLog;
import com.treinotracker.repository.ExerciseRepository;
import com.treinotracker.repository.SetLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class WorkoutService {

    private final ExerciseRepository exerciseRepository;
    private final SetLogRepository setLogRepository;

    public WorkoutService(ExerciseRepository exerciseRepository, SetLogRepository setLogRepository) {
        this.exerciseRepository = exerciseRepository;
        this.setLogRepository = setLogRepository;
    }

    public List<Exercise> getExercises() {
        return exerciseRepository.findAll();
    }

    public boolean exerciseExists(String name) {
        return exerciseRepository.existsByNameIgnoreCase(name);
    }

    @Transactional
    public Exercise addExercise(String name, String group) {
        if (exerciseExists(name)) {
            throw new IllegalArgumentException("Exercício já existe: " + name);
        }
        return exerciseRepository.save(new Exercise(name, group));
    }

    @Transactional
    public SetLog logSet(String exerciseName, int week, double weight, int reps, int setsCount) {
        Exercise exercise = exerciseRepository.findByNameIgnoreCase(exerciseName)
                .orElseThrow(() -> new IllegalArgumentException("Exercício não encontrado: " + exerciseName));
        SetLog setLog = new SetLog(exercise, week, weight, reps, setsCount, LocalDate.now());
        return setLogRepository.save(setLog);
    }

    public List<SetLog> getSetsFor(String exerciseName) {
        return setLogRepository.findByExercise_NameIgnoreCaseOrderByWeekAsc(exerciseName);
    }

    public List<WeekSummary> getProgression(String exerciseName) {
        TreeMap<Integer, SetLog> bestPerWeek = new TreeMap<>();
        for (SetLog set : getSetsFor(exerciseName)) {
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

    public boolean isProgressing(String exerciseName) {
        List<WeekSummary> progression = getProgression(exerciseName);
        if (progression.isEmpty()) {
            return false;
        }
        WeekSummary last = progression.get(progression.size() - 1);
        return last.trendPercent() != null && last.trendPercent() > 0;
    }
}
