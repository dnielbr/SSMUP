package com.br.ssmup.empresa.inspecao.dto;

import com.br.ssmup.empresa.inspecao.enums.StatusInspecao;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.List;

public record InspecaoRelatorioRequestDto(
        @NotBlank(message = "Objeitvo da inspecao é obrigatorio")
        String objetivoInspecao,
        @NotBlank(message = "Observações são obrigatorias")
        String observacoes,
        @NotNull(message = "Data da inspecao nao pode ser null")
        @PastOrPresent
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataInspecao,
        @NotNull(message = "O status da inspecao é obrigatorio")
        @Enumerated
        StatusInspecao statusInspecao,
        @NotNull(message = "O id da empresa é obrigatorio")
        Long empresaId,
        @NotNull(message = "Pelo menos um id de usuario deve ser fornecido")
        List<Long> usuariosId
) {
}