package com.treinotracker.repository;

import com.treinotracker.entity.SessaoExercicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessaoExercicioRepository extends JpaRepository<SessaoExercicio, Long> {

    List<SessaoExercicio> findByExerciseIdOrderBySemanaAsc(Long exerciseId);
}
