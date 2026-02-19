package com.br.ssmup.dto;

import com.br.ssmup.enums.StatusInspecao;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InspecaoRelatorioResponseDto(
        Long id,
        String objetivoInspecao,
        String observacoes,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataInspecao,
        StatusInspecao statusInspecao,
        Long empresaId,
        List<Long> usuariosId,
        LocalDateTime createdAt,
        LocalDateTime updateAt
) implements Serializable {
}
