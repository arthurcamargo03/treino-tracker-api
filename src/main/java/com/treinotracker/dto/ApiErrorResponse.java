package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        @Schema(description = "Momento em que o erro ocorreu") Instant timestamp,
        @Schema(description = "Status HTTP", example = "404") int status,
        @Schema(description = "Mensagem descrevendo o erro", example = "Exercício não encontrado: 999") String message,
        @Schema(description = "Mapa campo -> mensagem de validação; null quando não é erro de validação") Map<String, String> fieldErrors
) {
    public static ApiErrorResponse of(int status, String message) {
        return new ApiErrorResponse(Instant.now(), status, message, null);
    }

    public static ApiErrorResponse of(int status, String message, Map<String, String> fieldErrors) {
        return new ApiErrorResponse(Instant.now(), status, message, fieldErrors);
    }
}
