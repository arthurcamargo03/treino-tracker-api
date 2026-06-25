package com.treinotracker.service;

import com.treinotracker.entity.Settings;
import com.treinotracker.entity.WaterLog;
import com.treinotracker.repository.SettingsRepository;
import com.treinotracker.repository.WaterLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaterServiceTest {

    @Mock
    private WaterLogRepository waterLogRepository;

    @Mock
    private SettingsRepository settingsRepository;

    private WaterService waterService;

    @BeforeEach
    void setUp() {
        waterService = new WaterService(waterLogRepository, settingsRepository);
    }

    @Test
    void today_reusesExistingLog_whenAlreadyCreated() {
        LocalDate today = LocalDate.now();
        WaterLog existing = new WaterLog(today, 750, 3000);
        when(waterLogRepository.findByDate(today)).thenReturn(Optional.of(existing));

        WaterLog result = waterService.today();

        assertThat(result).isEqualTo(existing);
        verify(waterLogRepository, never()).save(any());
    }

    @Test
    void today_createsLogWithCurrentGoal_whenMissing() {
        LocalDate today = LocalDate.now();
        when(waterLogRepository.findByDate(today)).thenReturn(Optional.empty());
        when(settingsRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(new Settings(3200, 600)));
        when(waterLogRepository.save(any(WaterLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WaterLog result = waterService.today();

        assertThat(result.getDate()).isEqualTo(today);
        assertThat(result.getConsumedMl()).isZero();
        assertThat(result.getGoalMl()).isEqualTo(3200);
    }

    @Test
    void drink_addsCustomAmountToTodayLog() {
        LocalDate today = LocalDate.now();
        WaterLog existing = new WaterLog(today, 500, 3000);
        when(waterLogRepository.findByDate(today)).thenReturn(Optional.of(existing));
        when(waterLogRepository.save(existing)).thenReturn(existing);

        WaterLog result = waterService.drink(250);

        assertThat(result.getConsumedMl()).isEqualTo(750);
        verify(waterLogRepository).save(existing);
    }

    @Test
    void updateSettings_savesBothValuesAtomically() {
        Settings settings = new Settings(3000, 500);
        when(settingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));
        when(settingsRepository.save(settings)).thenReturn(settings);

        Settings result = waterService.updateSettings(3500, 700);

        assertThat(result.getDailyGoalMl()).isEqualTo(3500);
        assertThat(result.getBottleSizeMl()).isEqualTo(700);
        verify(settingsRepository).save(settings);
    }

    @Test
    void updateSettings_rejectsInvalidValues_beforeSaving() {
        assertThatThrownBy(() -> waterService.updateSettings(3000, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tamanho da garrafa");

        verify(settingsRepository, never()).save(any());
    }
}
