package com.br.ssmup.empresa.licensa.dto;

import com.br.ssmup.empresa.licensa.enums.StatusLicensa;

public record LicensaFilterDto(
    String numControle,
    String cnpj,
    StatusLicensa status
) {}
