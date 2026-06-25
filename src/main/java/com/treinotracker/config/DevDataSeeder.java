package com.treinotracker.config;

import com.treinotracker.entity.Exercise;
import com.treinotracker.entity.TrainingDay;
import com.treinotracker.repository.ExerciseRepository;
import com.treinotracker.service.WorkoutService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final WorkoutService workoutService;
    private final ExerciseRepository exerciseRepository;

    public DevDataSeeder(WorkoutService workoutService, ExerciseRepository exerciseRepository) {
        this.workoutService = workoutService;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public void run(String... args) {
        if (exerciseRepository.count() > 0) {
            return;
        }

        TrainingDay treinoA = workoutService.addTrainingDay("Treino A — Peito", DayOfWeek.MONDAY);
        TrainingDay treinoB = workoutService.addTrainingDay("Treino B — Posterior de coxa", DayOfWeek.WEDNESDAY);
        TrainingDay treinoC = workoutService.addTrainingDay("Treino C — Braços", DayOfWeek.FRIDAY);

        Exercise progressing = workoutService.addExercise("Supino reto", "Peito", treinoA.getId());
        workoutService.logSet(progressing.getId(), 1, 60.0, 10, 3);
        workoutService.logSet(progressing.getId(), 2, 63.0, 10, 3);
        workoutService.logSet(progressing.getId(), 3, 66.0, 10, 3);
        workoutService.logSet(progressing.getId(), 4, 70.0, 10, 3);

        Exercise strong = workoutService.addExercise("Levantamento terra", "Posterior de coxa", treinoB.getId());
        workoutService.logSet(strong.getId(), 1, 140.0, 5, 3);
        workoutService.logSet(strong.getId(), 2, 142.0, 5, 3);
        workoutService.logSet(strong.getId(), 3, 141.0, 5, 3);

        Exercise stagnant = workoutService.addExercise("Rosca direta", "Bíceps", treinoC.getId());
        workoutService.logSet(stagnant.getId(), 1, 20.0, 12, 3);
        workoutService.logSet(stagnant.getId(), 2, 20.0, 12, 3);
        workoutService.logSet(stagnant.getId(), 3, 20.0, 12, 3);
        workoutService.logSet(stagnant.getId(), 4, 20.0, 12, 3);
    }
}
