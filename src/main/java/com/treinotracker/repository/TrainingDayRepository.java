package com.treinotracker.repository;

import com.treinotracker.entity.TrainingDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingDayRepository extends JpaRepository<TrainingDay, Long> {

    Optional<TrainingDay> findByNameIgnoreCase(String name);
}
