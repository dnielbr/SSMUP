package com.br.ssmup.empresa.cnae.mapper;

import com.br.ssmup.empresa.cnae.dto.CnaeResponseDto;

import com.br.ssmup.empresa.cnae.entity.Cnae;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CnaeMapper {
    CnaeResponseDto toResponse(Cnae cnae);
}
