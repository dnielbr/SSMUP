package com.br.ssmup.dto;

import java.io.Serializable;

public record ResponsavelResponseDto(
    Long id,
    String nome,
    String cpf,
    String rg,
    String escolaridade,
    String formacao,
    String especializacao,
    String registroConselho
) implements Serializable {
}
