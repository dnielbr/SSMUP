package com.br.ssmup.empresa.cadastro.dto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelCadastroDto;
import com.br.ssmup.empresa.endereco.dto.EnderecoCadastroDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.validator.constraints.br.CNPJ;

import java.time.LocalDate;

public record EmpresaCadastroDto(
        @NotBlank(message = "Razão social é obrigatoria")
        String razaoSocial,
        @NotBlank(message = "Nome fantasia é obrigatorio")
        String nomeFantasia,
        @CNPJ
        String cnpj,
        @NotBlank(message = "Email é obrigatorio")
        @Email(message = "Email Inválido")
        String email,
        String inscricaoEstadual,
        @NotBlank(message = "Atividade da firma é obrigatoria")
        String atividadeFirma,
        @NotBlank(message = "O código do CNAE é obrigatório")
        String cnaeCodigo,
        String subAtividade,
        @NotNull(message = "Data inicio de funcionamento não pode ser nula")
        @PastOrPresent
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataInicioFuncionamento,
        @Valid
        EnderecoCadastroDto endereco,
        @Valid
        ResponsavelCadastroDto responsavel
) {
}
