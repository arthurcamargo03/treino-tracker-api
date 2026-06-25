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
                .orElseGet(() -> createTodayLog(date));
    }

    @Transactional
    public WaterBottleProgress todayProgress() {
        return bottleProgress(today());
    }

    @Transactional
    public WaterLog drinkBottle() {
        return drink(getSettings().getBottleSizeMl());
    }

    @Transactional
    public WaterBottleProgress drinkBottleProgress() {
        return bottleProgress(drinkBottle());
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
    public WaterBottleProgress drinkProgress(int ml) {
        return bottleProgress(drink(ml));
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

    @Transactional
    public Settings updateSettings(int dailyGoalMl, int bottleSizeMl) {
        if (dailyGoalMl <= 0) {
            throw new IllegalArgumentException("Meta diária deve ser positiva: " + dailyGoalMl);
        }
        if (bottleSizeMl <= 0) {
            throw new IllegalArgumentException("Tamanho da garrafa deve ser positivo: " + bottleSizeMl);
        }
        Settings settings = getSettings();
        settings.setDailyGoalMl(dailyGoalMl);
        settings.setBottleSizeMl(bottleSizeMl);
        return settingsRepository.save(settings);
    }

    public Settings getSettings() {
        return settingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(this::createDefaultSettings);
    }

    public WaterBottleProgress bottleProgress(WaterLog waterLog) {
        int bottleSizeMl = getSettings().getBottleSizeMl();
        int bottlesForGoal = bottlesForGoal(waterLog.getGoalMl(), bottleSizeMl);
        int remainingBottles = remainingBottles(waterLog.getGoalMl(), waterLog.getConsumedMl(), bottleSizeMl);
        return new WaterBottleProgress(waterLog, bottleSizeMl, bottlesForGoal, remainingBottles);
    }

    int bottlesForGoal(int goalMl, int bottleSizeMl) {
        return (int) Math.ceil(goalMl / (double) bottleSizeMl);
    }

    int remainingBottles(int goalMl, int consumedMl, int bottleSizeMl) {
        return Math.max(0, (int) Math.ceil((goalMl - consumedMl) / (double) bottleSizeMl));
    }

    private WaterLog createTodayLog(LocalDate date) {
        synchronized (this) {
            return waterLogRepository.findByDate(date)
                    .orElseGet(() -> waterLogRepository.save(new WaterLog(date, 0, getSettings().getDailyGoalMl())));
        }
    }

    private Settings createDefaultSettings() {
        synchronized (this) {
            return settingsRepository.findFirstByOrderByIdAsc()
                    .orElseGet(() -> settingsRepository.save(new Settings(DEFAULT_DAILY_GOAL_ML, DEFAULT_BOTTLE_SIZE_ML)));
        }
    }
}
