package com.br.ssmup.empresa.responsavel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record ResponsavelCadastroDto(
        @NotBlank(message = "Nome é obrigatorio")
        String nome,
        @NotBlank(message = "Cpf é obrigatorio")
        @CPF(message = "Cpf Inválido")
        String cpf,
        @NotBlank(message = "Rg é obrigatorio")
        String rg,
        String escolaridade,
        String formacao,
        String especializacao,
        String registroConselho
) {
}
