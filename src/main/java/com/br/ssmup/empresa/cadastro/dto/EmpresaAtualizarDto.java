package com.br.ssmup.empresa.cadastro.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record EmpresaAtualizarDto(
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String email,
        String inscricaoEstadual,
        String atividadeFirma,
        String subAtividade,
        @PastOrPresent
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataInicioFuncionamento
) {
}
