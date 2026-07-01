package com.treinotracker.config;

import com.treinotracker.entity.Exercise;
import com.treinotracker.entity.Serie;
import com.treinotracker.entity.TrainingDay;
import com.treinotracker.repository.ExerciseRepository;
import com.treinotracker.service.WorkoutService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

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
        for (int semana = 1; semana <= 6; semana++) {
            double base = 60.0 + (semana - 1) * 2.5;
            registrarSessao(supino.getId(), semana, base, 10, 3, 2.5);
        }

        Exercise terra = workoutService.addExercise("Levantamento terra", "Posterior de coxa", treinoB.getId());
        for (int semana = 1; semana <= 4; semana++) {
            double base = 140.0 + (semana - 1) * 5.0;
            registrarSessao(terra.getId(), semana, base, 5, 3, 5.0);
        }

        Exercise rosca = workoutService.addExercise("Rosca direta", "Bíceps", treinoC.getId(), 2);
        for (int semana = 1; semana <= 3; semana++) {
            double base = 18.0 + (semana - 1) * 2.0;
            registrarSessao(rosca.getId(), semana, base, 12, 2, 2.0);
        }
    }

    // Cada sessão tem `seriesValidas` séries: a 1ª com a carga base e as seguintes
    // um pouco mais leves (fadiga), todas subindo semana a semana.
    private void registrarSessao(Long exerciseId, int semana, double base, int reps, int seriesValidas, double decremento) {
        List<Serie> series = new ArrayList<>();
        for (int posicao = 1; posicao <= seriesValidas; posicao++) {
            Serie serie = new Serie();
            serie.setPosicao(posicao);
            serie.setCarga(base - (posicao - 1) * decremento);
            serie.setReps(reps);
            series.add(serie);
        }
        workoutService.registrarSessao(exerciseId, semana, series);
    }
}
