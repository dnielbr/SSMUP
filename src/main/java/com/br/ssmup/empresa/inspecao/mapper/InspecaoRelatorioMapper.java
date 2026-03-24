package com.br.ssmup.empresa.inspecao.mapper;

import com.br.ssmup.empresa.inspecao.dto.InspecaoRelatorioRequestDto;
import com.br.ssmup.empresa.inspecao.dto.InspecaoRelatorioResponseDto;
import com.br.ssmup.empresa.inspecao.dto.InspecaoRelatorioUpdateDto;
import com.br.ssmup.empresa.inspecao.entity.InspecaoRelatorio;
import com.br.ssmup.auth.usuario.entity.Usuario;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface InspecaoRelatorioMapper {
    InspecaoRelatorio toEntity(InspecaoRelatorioRequestDto inspecaoRelatorioRequestDto);

    @Mapping(source = "empresa.id", target = "empresaId")
    @Mapping(source = "usuarios", target = "usuariosId", qualifiedByName = "usuariosParaIds")
    InspecaoRelatorioResponseDto toDto(InspecaoRelatorio inspecaoRelatorio);

    @Named("usuariosParaIds")
    default List<Long> usuariosParaIds(List<Usuario> usuarios) {
        if (usuarios == null) { return Collections.emptyList(); }
        return usuarios.stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "usuarios", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    void updateFromDto(InspecaoRelatorioUpdateDto dto, @MappingTarget InspecaoRelatorio entity);
}
