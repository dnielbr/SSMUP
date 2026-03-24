package com.br.ssmup.empresa.endereco.dto;

import com.br.ssmup.empresa.endereco.enums.UnidadeFederativa;

import java.io.Serializable;

public record EnderecoResponseDto(
        Long id,
        String rua,
        String numero,
        String bairro,
        String cep,
        String municipio,
        UnidadeFederativa uf,
        String telefone
) implements Serializable {
}
