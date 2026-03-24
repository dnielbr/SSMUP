package com.br.ssmup.empresa.cnae.dto;

import com.br.ssmup.empresa.cnae.enums.RiscoSanitario;

import java.io.Serializable;

public record CnaeResponseDto(
        String codigo,
        String descricao,
        RiscoSanitario risco
) implements Serializable {
}
