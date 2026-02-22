package com.br.ssmup.dto;

import com.br.ssmup.enums.RiscoSanitario;

import java.time.LocalDate;

public record EmpresaFilterDto(
        Long id,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String email,
        String inscricaoEstadual,
        String atividadeFirma,
        String subAtividade,
        LocalDate dataInicioFuncionamento,
        Boolean ativo,
        Boolean inspecao,
        RiscoSanitario risco
) {
}
