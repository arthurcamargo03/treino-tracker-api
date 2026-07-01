package com.treinotracker.repository;

import com.treinotracker.entity.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    @Query("select s from Serie s join fetch s.sessaoExercicio se "
            + "where se.exercise.id = :exerciseId order by se.semana asc, s.posicao asc")
    List<Serie> findAllByExerciseId(@Param("exerciseId") Long exerciseId);

    @Modifying
    @Query("delete from Serie s where s.sessaoExercicio.id in "
            + "(select se.id from SessaoExercicio se where se.exercise.id = :exerciseId)")
    void deleteByExerciseId(@Param("exerciseId") Long exerciseId);
}
