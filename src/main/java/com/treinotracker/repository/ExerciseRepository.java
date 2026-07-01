package com.treinotracker.repository;

import com.treinotracker.entity.Exercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    Optional<Exercise> findByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = "trainingDay")
    @Query("select e from Exercise e order by e.id")
    List<Exercise> findAllWithTrainingDay();

    @EntityGraph(attributePaths = "trainingDay")
    @Query("select e from Exercise e where e.id = :id")
    Optional<Exercise> findByIdWithTrainingDay(@Param("id") Long id);

    List<Exercise> findByTrainingDayId(Long trainingDayId);
}
