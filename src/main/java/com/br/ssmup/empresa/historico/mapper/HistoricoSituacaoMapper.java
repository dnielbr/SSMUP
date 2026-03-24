package com.br.ssmup.empresa.historico.mapper;

import com.br.ssmup.empresa.historico.dto.HistoricoSituacaoResponseDto;
import com.br.ssmup.empresa.historico.entity.HistoricoSituacao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistoricoSituacaoMapper {
    @Mapping(source = "usuario.nome", target = "usuarioResponsavel")
    HistoricoSituacaoResponseDto toDto(HistoricoSituacao h);
}
