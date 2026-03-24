package com.br.ssmup.empresa.cadastro.mapper;

import com.br.ssmup.empresa.cadastro.dto.EmpresaAtualizarDto;
import com.br.ssmup.empresa.cadastro.dto.EmpresaCadastroDto;
import com.br.ssmup.empresa.cadastro.dto.EmpresaResponseDto;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.endereco.mapper.EnderecoMapper;
import com.br.ssmup.empresa.responsavel.mapper.ResponsavelMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {EnderecoMapper.class, ResponsavelMapper.class})
public interface EmpresaMapper {

    @Mapping(target = "cnaePrincipal", ignore = true)
    @Mapping(source = "endereco", target = "endereco")
    @Mapping(source = "responsavel", target = "responsavel")
    Empresa toEntity(EmpresaCadastroDto dto);

    @Mapping(source = "cnaePrincipal", target = "cnae")
    EmpresaResponseDto toResponse(Empresa empresa);

    @AfterMapping
    default void afterMapping(@MappingTarget Empresa empresa) {
        empresa.adicionarEndereco(empresa.getEndereco());
    }

    EmpresaAtualizarDto toUpdate(Empresa empresa);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "inspecao", ignore = true)
    @Mapping(target = "licensasSanitarias", ignore = true)
    @Mapping(target = "localizacao", ignore = true)
    @Mapping(target = "cnaePrincipal", ignore = true)
    @Mapping(target = "endereco", ignore = true)
    @Mapping(target = "responsavel", ignore = true)
    void updateFromDto(EmpresaAtualizarDto dto, @MappingTarget Empresa empresa);
}
