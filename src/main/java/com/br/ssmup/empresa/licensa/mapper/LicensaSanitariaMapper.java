package com.br.ssmup.empresa.licensa.mapper;
import com.br.ssmup.empresa.cadastro.mapper.EmpresaMapper;

import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaCadastroDto;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaResponseDto;
import com.br.ssmup.empresa.licensa.entity.LicensaSanitaria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EmpresaMapper.class})
public interface LicensaSanitariaMapper {
    LicensaSanitaria toEntity(LicensaSanitariaCadastroDto dto);
    LicensaSanitariaResponseDto toResponse(LicensaSanitaria entity);
}
