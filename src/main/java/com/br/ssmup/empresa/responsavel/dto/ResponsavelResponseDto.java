package com.br.ssmup.empresa.responsavel.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ResponsavelResponseDto(
    Long id,
    String nome,
    String cpf,
    String rg,
    String escolaridade,
    String formacao,
    String especializacao,
    String registroConselho,
    LocalDateTime criadoEm,
    LocalDateTime atualizadoEm,
    String criadoPor,
    String atualizadoPor
) implements Serializable {
}
