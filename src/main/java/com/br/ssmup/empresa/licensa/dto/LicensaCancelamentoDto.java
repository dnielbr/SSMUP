package com.br.ssmup.empresa.licensa.dto;

import jakarta.validation.constraints.NotBlank;

public record LicensaCancelamentoDto(
        @NotBlank(message = "Motivo do cancelamento é obrigatório")
        String motivo
) {
}
