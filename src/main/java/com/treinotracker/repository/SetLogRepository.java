package com.treinotracker.repository;

import com.treinotracker.entity.SetLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SetLogRepository extends JpaRepository<SetLog, Long> {

    List<SetLog> findByExerciseIdOrderByWeekAsc(Long exerciseId);

    @Modifying
    @Query("delete from SetLog s where s.exercise.id = :exerciseId")
    void deleteByExerciseId(@Param("exerciseId") Long exerciseId);
}
