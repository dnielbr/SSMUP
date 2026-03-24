package com.br.ssmup.empresa.endereco.mapper;

import com.br.ssmup.empresa.endereco.dto.EnderecoAtualizarDto;
import com.br.ssmup.empresa.endereco.dto.EnderecoCadastroDto;
import com.br.ssmup.empresa.endereco.dto.EnderecoResponseDto;
import com.br.ssmup.empresa.endereco.entity.Endereco;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EnderecoMapper {
    Endereco toEntity(EnderecoCadastroDto dto);
    EnderecoResponseDto toResponse(Endereco endereco);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    void updateFromDto(EnderecoAtualizarDto dto, @MappingTarget Endereco endereco);
}
