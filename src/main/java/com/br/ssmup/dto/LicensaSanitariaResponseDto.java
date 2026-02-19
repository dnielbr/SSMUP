package com.br.ssmup.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LicensaSanitariaResponseDto(
        Long id,
        String numControle,
        LocalDateTime dataEmissao,
        LocalDate dataValidade,
        boolean status,
        EmpresaResponseDto empresa
) implements Serializable {
}
