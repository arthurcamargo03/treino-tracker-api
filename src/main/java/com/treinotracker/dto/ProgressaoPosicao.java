package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProgressaoPosicao(
        @Schema(description = "Posição da série dentro da sessão, começando em 1", example = "1") int posicao,
        @Schema(description = "Evolução da posição ao longo das semanas") List<PontoSerie> pontos
) {
}
