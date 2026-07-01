package com.treinotracker.dto;

import com.treinotracker.entity.SessaoExercicio;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record SessaoResponse(
        @Schema(description = "Id da sessão", example = "1") Long id,
        @Schema(description = "Id do exercício", example = "1") Long exerciseId,
        @Schema(description = "Semana do ciclo de treino", example = "1") int semana,
        @Schema(description = "Data em que a sessão foi registrada") LocalDate data
) {
    public static SessaoResponse from(SessaoExercicio sessao) {
        return new SessaoResponse(
                sessao.getId(),
                sessao.getExercise().getId(),
                sessao.getSemana(),
                sessao.getData()
        );
    }
}
