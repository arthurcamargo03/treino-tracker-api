package com.treinotracker.controller;

import com.treinotracker.entity.Settings;
import com.treinotracker.entity.WaterLog;
import com.treinotracker.service.WaterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WaterController.class)
class WaterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaterService waterService;

    @Test
    void today_returnsHydrationProgress() throws Exception {
        when(waterService.today()).thenReturn(new WaterLog(LocalDate.of(2026, 6, 25), 1500, 3000));

        mockMvc.perform(get("/api/water/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consumedMl").value(1500))
                .andExpect(jsonPath("$.goalMl").value(3000))
                .andExpect(jsonPath("$.percent").value(50.0))
                .andExpect(jsonPath("$.goalReached").value(false));
    }

    @Test
    void drink_usesBottleSize_whenBodyIsMissing() throws Exception {
        when(waterService.drinkBottle()).thenReturn(new WaterLog(LocalDate.of(2026, 6, 25), 500, 3000));

        mockMvc.perform(post("/api/water/drink"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consumedMl").value(500));
    }

    @Test
    void updateSettings_returnsUpdatedSettings() throws Exception {
        when(waterService.updateSettings(eq(3500), eq(700))).thenReturn(new Settings(3500, 700));

        mockMvc.perform(put("/api/water/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dailyGoalMl\":3500,\"bottleSizeMl\":700}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyGoalMl").value(3500))
                .andExpect(jsonPath("$.bottleSizeMl").value(700));
    }

    @Test
    void updateSettings_returns400_whenInvalid() throws Exception {
        mockMvc.perform(put("/api/water/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dailyGoalMl\":0,\"bottleSizeMl\":700}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.dailyGoalMl").exists());
    }
}
