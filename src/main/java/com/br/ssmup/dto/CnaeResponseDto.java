package com.br.ssmup.dto;

import com.br.ssmup.enums.RiscoSanitario;

import java.io.Serializable;

public record CnaeResponseDto(
        String codigo,
        String descricao,
        RiscoSanitario risco
) implements Serializable {
}
