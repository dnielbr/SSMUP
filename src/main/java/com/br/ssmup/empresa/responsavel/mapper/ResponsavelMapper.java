package com.br.ssmup.empresa.responsavel.mapper;

import com.br.ssmup.empresa.responsavel.dto.ResponsavelAtualizarDto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelCadastroDto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelResponseDto;
import com.br.ssmup.empresa.responsavel.entity.Responsavel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ResponsavelMapper {
    Responsavel toEntity(ResponsavelCadastroDto dto);
    ResponsavelResponseDto toResponse(Responsavel responsavel);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresas", ignore = true)
    void updateFromDto(ResponsavelAtualizarDto dto, @MappingTarget Responsavel responsavel);
}
