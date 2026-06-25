package com.treinotracker.service;

import com.treinotracker.entity.Settings;
import com.treinotracker.entity.WaterLog;
import com.treinotracker.repository.SettingsRepository;
import com.treinotracker.repository.WaterLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class WaterService {

    private static final int DEFAULT_DAILY_GOAL_ML = 3000;
    private static final int DEFAULT_BOTTLE_SIZE_ML = 500;

    private final WaterLogRepository waterLogRepository;
    private final SettingsRepository settingsRepository;

    public WaterService(WaterLogRepository waterLogRepository, SettingsRepository settingsRepository) {
        this.waterLogRepository = waterLogRepository;
        this.settingsRepository = settingsRepository;
    }

    @Transactional
    public WaterLog today() {
        LocalDate date = LocalDate.now();
        return waterLogRepository.findByDate(date)
                .orElseGet(() -> waterLogRepository.save(new WaterLog(date, 0, getSettings().getDailyGoalMl())));
    }

    @Transactional
    public WaterLog drinkBottle() {
        return drink(getSettings().getBottleSizeMl());
    }

    @Transactional
    public WaterLog drink(int ml) {
        if (ml <= 0) {
            throw new IllegalArgumentException("Quantidade de água deve ser positiva: " + ml);
        }
        WaterLog log = today();
        log.setConsumedMl(log.getConsumedMl() + ml);
        return waterLogRepository.save(log);
    }

    @Transactional
    public Settings updateDailyGoal(int dailyGoalMl) {
        if (dailyGoalMl <= 0) {
            throw new IllegalArgumentException("Meta diária deve ser positiva: " + dailyGoalMl);
        }
        Settings settings = getSettings();
        settings.setDailyGoalMl(dailyGoalMl);
        return settingsRepository.save(settings);
    }

    @Transactional
    public Settings updateBottleSize(int bottleSizeMl) {
        if (bottleSizeMl <= 0) {
            throw new IllegalArgumentException("Tamanho da garrafa deve ser positivo: " + bottleSizeMl);
        }
        Settings settings = getSettings();
        settings.setBottleSizeMl(bottleSizeMl);
        return settingsRepository.save(settings);
    }

    public Settings getSettings() {
        return settingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> settingsRepository.save(new Settings(DEFAULT_DAILY_GOAL_ML, DEFAULT_BOTTLE_SIZE_ML)));
    }
}
