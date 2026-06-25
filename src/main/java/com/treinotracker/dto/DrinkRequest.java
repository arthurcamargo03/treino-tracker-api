package com.treinotracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record DrinkRequest(
        @Schema(description = "Quantidade em ml a registrar; se omitido, usa o tamanho de garrafa configurado", example = "500") @Positive Integer ml
) {
}
