package com.br.ssmup.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDate;

public record EmpresaResponseDto(
        Long id,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String email,
        String inscricaoEstadual,
        String atividadeFirma,
        String subAtividade,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataInicioFuncionamento,
        boolean ativo,
        boolean inspecao,
        EnderecoResponseDto endereco,
        ResponsavelResponseDto responsavel,
        CnaeResponseDto cnae
) implements Serializable {
}
