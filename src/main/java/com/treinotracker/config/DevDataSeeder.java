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

        Exercise supino = workoutService.addExercise("Supino reto", "Peito", treinoA.getId());
        workoutService.logSet(supino.getId(), 1, 60.0, 10, 3);
        workoutService.logSet(supino.getId(), 2, 62.5, 10, 3);
        workoutService.logSet(supino.getId(), 3, 65.0, 10, 3);
        workoutService.logSet(supino.getId(), 4, 67.5, 10, 3);
        workoutService.logSet(supino.getId(), 5, 70.0, 10, 3);
        workoutService.logSet(supino.getId(), 6, 72.5, 10, 3);

        Exercise terra = workoutService.addExercise("Levantamento terra", "Posterior de coxa", treinoB.getId());
        workoutService.logSet(terra.getId(), 1, 140.0, 5, 3);
        workoutService.logSet(terra.getId(), 2, 145.0, 5, 3);
        workoutService.logSet(terra.getId(), 3, 150.0, 5, 3);
        workoutService.logSet(terra.getId(), 4, 155.0, 5, 3);

        Exercise rosca = workoutService.addExercise("Rosca direta", "Bíceps", treinoC.getId());
        workoutService.logSet(rosca.getId(), 1, 18.0, 12, 3);
        workoutService.logSet(rosca.getId(), 2, 20.0, 12, 3);
        workoutService.logSet(rosca.getId(), 3, 22.0, 12, 3);
    }
}
