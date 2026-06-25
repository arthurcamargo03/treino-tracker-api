package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record WeekSummary(
        @Schema(description = "Semana do ciclo de treino", example = "1") int week,
        @Schema(description = "Carga da melhor série da semana, em kg", example = "60.0") double weight,
        @Schema(description = "Repetições da melhor série da semana", example = "10") int reps,
        @Schema(description = "Número de séries da melhor série da semana", example = "3") int sets,
        @Schema(description = "Volume da melhor série da semana", example = "1800.0") double volume,
        @Schema(description = "1RM estimado pela fórmula de Epley", example = "80.0") double estimated1RM,
        @Schema(description = "Variação percentual do 1RM em relação à semana anterior; null na primeira semana", example = "8.33") Double trendPercent
) {
}
