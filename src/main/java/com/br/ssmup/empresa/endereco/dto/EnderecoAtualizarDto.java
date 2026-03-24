package com.br.ssmup.empresa.endereco.dto;

import com.br.ssmup.empresa.endereco.enums.UnidadeFederativa;

public record EnderecoAtualizarDto(
        String rua,
        String numero,
        String bairro,
        String cep,
        String municipio,
        UnidadeFederativa uf,
        String telefone
) {

}
