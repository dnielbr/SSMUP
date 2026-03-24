package com.br.ssmup.empresa.endereco.dto;

import com.br.ssmup.empresa.endereco.entity.Endereco;
import com.br.ssmup.empresa.endereco.enums.UnidadeFederativa;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnderecoCadastroDto(
        @NotBlank(message = "Rua é obrigatoria")
        String rua,
        String numero,
        @NotBlank(message = "Bairro é obrigatorio")
        String bairro,
        @NotBlank(message = "Cep é obrigatorio")
        String cep,
        @NotBlank(message = "Municipio é obrigatorio")
        String municipio,
        @NotNull(message = "Unidade federativa é obrigatoria")
        @Enumerated
        UnidadeFederativa uf,
        String telefone
) {
}
