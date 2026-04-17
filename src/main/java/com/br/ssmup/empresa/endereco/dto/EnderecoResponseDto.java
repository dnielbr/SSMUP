package com.br.ssmup.empresa.endereco.dto;

import com.br.ssmup.empresa.endereco.enums.UnidadeFederativa;

import java.io.Serializable;
import java.time.LocalDateTime;

public record EnderecoResponseDto(
        Long id,
        String rua,
        String numero,
        String bairro,
        String cep,
        String municipio,
        UnidadeFederativa uf,
        String telefone,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        String criadoPor,
        String atualizadoPor
) implements Serializable {
}
