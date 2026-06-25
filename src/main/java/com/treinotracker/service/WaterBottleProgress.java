package com.treinotracker.service;

import com.treinotracker.entity.WaterLog;

public record WaterBottleProgress(
        WaterLog waterLog,
        int bottleSizeMl,
        int bottlesForGoal,
        int remainingBottles
) {
    public int completedBottles() {
        return Math.max(0, bottlesForGoal - remainingBottles);
    }
}
