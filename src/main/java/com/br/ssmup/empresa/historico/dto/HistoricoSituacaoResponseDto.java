package com.br.ssmup.empresa.historico.dto;

import com.br.ssmup.empresa.historico.enums.TipoSituacao;

import java.io.Serializable;
import java.time.LocalDateTime;

public record HistoricoSituacaoResponseDto(
        Long id,
        String motivo,
        TipoSituacao tipoSituacao,
        LocalDateTime data,
        String usuarioResponsavel,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        String criadoPor,
        String atualizadoPor
) implements Serializable {
}
