package com.br.ssmup.empresa.cadastro.dto;

import java.io.Serializable;

public record EmpresaRiscoResponseDto(
        long qtEmpresasBaixoRisco,
        long qtEmpresasRiscoMedio,
        long qtEmpresasRiscoAlto
) implements Serializable {
}
