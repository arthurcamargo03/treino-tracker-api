package com.treinotracker.repository;

import com.treinotracker.entity.SetLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetLogRepository extends JpaRepository<SetLog, Long> {

    List<SetLog> findByExercise_NameIgnoreCaseOrderByWeekAsc(String exerciseName);
}
