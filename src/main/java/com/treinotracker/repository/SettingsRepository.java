package com.treinotracker.repository;

import com.treinotracker.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {

    Optional<Settings> findFirstByOrderByIdAsc();
}
