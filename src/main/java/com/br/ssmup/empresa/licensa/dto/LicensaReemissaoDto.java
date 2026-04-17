package com.br.ssmup.empresa.licensa.dto;

import jakarta.validation.constraints.NotBlank;

public record LicensaReemissaoDto(
        @NotBlank(message = "Número de controle é obrigatório")
        String numControle,
        @NotBlank(message = "Motivo do cancelamento é obrigatório")
        String motivoCancelamento
) {
}
