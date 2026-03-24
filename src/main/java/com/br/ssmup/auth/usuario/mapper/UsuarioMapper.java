package com.br.ssmup.auth.usuario.mapper;

import com.br.ssmup.auth.usuario.dto.UsuarioAtualizarDto;
import com.br.ssmup.auth.usuario.dto.UsuarioCadastroDto;
import com.br.ssmup.auth.usuario.dto.UsuarioResponseDto;
import com.br.ssmup.auth.usuario.entity.Usuario;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    Usuario toEntity(UsuarioCadastroDto dto);
    UsuarioResponseDto toResponse(Usuario entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "tokenAtivacao", ignore = true)
    @Mapping(target = "dataExpiracaoToken", ignore = true)
    @Mapping(target = "emailVerificado", ignore = true)
    void updateFromDto(UsuarioAtualizarDto dto, @MappingTarget Usuario usuario);
}
