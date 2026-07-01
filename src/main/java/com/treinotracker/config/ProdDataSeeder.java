package com.treinotracker.config;

import com.treinotracker.repository.ExerciseRepository;
import com.treinotracker.service.WorkoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Popula o banco de produção com um histórico de exemplo apenas na primeira
 * execução (banco vazio), para que o link não abra sem nenhum dado. Em deploys
 * seguintes o banco já tem exercícios e nada é reinserido.
 */
@Component
@Profile("prod")
public class ProdDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdDataSeeder.class);

    private final WorkoutService workoutService;
    private final ExerciseRepository exerciseRepository;

    public ProdDataSeeder(WorkoutService workoutService, ExerciseRepository exerciseRepository) {
        this.workoutService = workoutService;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public void run(String... args) {
        if (exerciseRepository.count() > 0) {
            return;
        }
        log.info("Banco de produção vazio: populando histórico de exemplo.");
        SampleData.seed(workoutService);
    }
}
