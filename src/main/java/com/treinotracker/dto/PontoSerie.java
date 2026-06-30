package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PontoSerie(
        @Schema(description = "Semana do ciclo de treino", example = "1") int semana,
        @Schema(description = "Carga registrada na série, em kg", example = "60.0") double carga,
        @Schema(description = "Repetições registradas na série", example = "10") int reps,
        @Schema(description = "1RM estimado pela fórmula de Epley", example = "80.0") double estimated1RM,
        @Schema(description = "Variação percentual do 1RM frente à semana anterior na mesma posição; "
                + "null quando a posição não existia na semana anterior (primeira vez)", example = "8.33")
        Double trendPercent
) {
}
