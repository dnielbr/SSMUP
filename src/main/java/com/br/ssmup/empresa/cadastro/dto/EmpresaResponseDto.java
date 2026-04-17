package com.br.ssmup.empresa.cadastro.dto;
import com.br.ssmup.empresa.cnae.dto.CnaeResponseDto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelResponseDto;
import com.br.ssmup.empresa.endereco.dto.EnderecoResponseDto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
//        boolean inspecao,
        EnderecoResponseDto endereco,
        ResponsavelResponseDto responsavel,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        String criadoPor,
        String atualizadoPor
//        CnaeResponseDto cnae
) implements Serializable {
}
