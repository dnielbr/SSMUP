package com.br.ssmup.empresa.inspecao.dto;

import com.br.ssmup.empresa.inspecao.enums.StatusInspecao;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.List;

public record InspecaoRelatorioUpdateDto(
        String objetivoInspecao,
        String observacoes,
        @PastOrPresent
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataInspecao,
        StatusInspecao statusInspecao,
        List<Long> usuariosId
) {
}
