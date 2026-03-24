package com.br.ssmup.empresa.cadastro.dto;

import com.br.ssmup.empresa.cnae.enums.RiscoSanitario;

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
