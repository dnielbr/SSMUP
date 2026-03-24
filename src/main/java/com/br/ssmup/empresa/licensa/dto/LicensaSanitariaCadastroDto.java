package com.br.ssmup.empresa.licensa.dto;

import jakarta.validation.constraints.NotBlank;

public record LicensaSanitariaCadastroDto(
        @NotBlank(message = "Número de controle é obrigatorio")
        String numControle
) {
}
