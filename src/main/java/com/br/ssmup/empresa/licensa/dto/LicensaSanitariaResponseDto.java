package com.br.ssmup.empresa.licensa.dto;
import com.br.ssmup.empresa.cadastro.dto.EmpresaResponseDto;
import com.br.ssmup.empresa.licensa.enums.StatusLicensa;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LicensaSanitariaResponseDto(
        Long id,
        String numControle,
        LocalDateTime dataEmissao,
        LocalDate dataValidade,
        StatusLicensa status,
        String motivoCancelamento,
        Long licensaSubstitutaId,
        EmpresaResponseDto empresa,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        String criadoPor,
        String atualizadoPor
) implements Serializable {
}
