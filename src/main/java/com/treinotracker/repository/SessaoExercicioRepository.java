package com.treinotracker.repository;

import com.treinotracker.entity.SessaoExercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessaoExercicioRepository extends JpaRepository<SessaoExercicio, Long> {

    List<SessaoExercicio> findByExerciseIdOrderBySemanaAsc(Long exerciseId);

    @Modifying
    @Query("delete from SessaoExercicio se where se.exercise.id = :exerciseId")
    void deleteByExerciseId(@Param("exerciseId") Long exerciseId);
}
