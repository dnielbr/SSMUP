package com.br.ssmup.empresa.historico.dto;

import jakarta.validation.constraints.NotBlank;

public record HistoricoSituacaoRequestDto(
        @NotBlank(message = "Motivo è obrigatorio para auditoria") String motivo) {
}
