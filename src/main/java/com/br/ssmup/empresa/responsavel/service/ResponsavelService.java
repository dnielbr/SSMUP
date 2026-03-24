package com.br.ssmup.empresa.responsavel.service;

import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.empresa.cadastro.dto.EmpresaResponseDto;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.cadastro.mapper.EmpresaMapper;
import com.br.ssmup.empresa.cadastro.repository.EmpresaRepository;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelAtualizarDto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelCadastroDto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelResponseDto;
import com.br.ssmup.empresa.responsavel.entity.Responsavel;
import com.br.ssmup.empresa.responsavel.mapper.ResponsavelMapper;
import com.br.ssmup.empresa.responsavel.repository.ResponsavelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import static com.br.ssmup.core.util.CacheNames.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;
    private final ResponsavelMapper responsavelMapper;
    private final EmpresaMapper empresaMapper;
    private final EmpresaRepository empresaRepository;

    public List<ResponsavelResponseDto> listarResponsaveis() {
        return responsavelRepository.findAll().stream()
                .map(responsavelMapper::toResponse)
                .toList();
    }

    public List<EmpresaResponseDto> listarEmpresasResponsaveis(Long id) {
        Responsavel responsavel = responsavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsavel não encontrado"));
        return responsavel.getEmpresas().stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

    @Transactional
    public ResponsavelResponseDto salvar(ResponsavelCadastroDto dto) {
        Responsavel responsavel = responsavelMapper.toEntity(dto);
        responsavelRepository.save(responsavel);
        return responsavelMapper.toResponse(responsavel);
    }

    public ResponsavelResponseDto buscarResponsavelById(Long id) {
        Responsavel responsavel = responsavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsavel não encontrado"));
        return responsavelMapper.toResponse(responsavel);
    }

    public ResponsavelResponseDto buscarResponsavelByCpf(String cpf) {
        Responsavel responsavel = responsavelRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Responsavel nao encontrado para esse CPF"));
        return responsavelMapper.toResponse(responsavel);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = EMPRESAS, key = "#empresaId"),
            @CacheEvict(cacheNames = EMPRESAS_PAGEABLE, allEntries = true),
            @CacheEvict(cacheNames = EMPRESAS_PAGEABLE_FILTER, allEntries = true)
    })
    @Transactional
    public ResponsavelResponseDto atualizarResponsavel(Long empresaId, ResponsavelAtualizarDto dto) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
        Responsavel responsavel = empresa.getResponsavel();

        responsavelMapper.updateFromDto(dto, responsavel);

        empresaRepository.save(empresa);
        return responsavelMapper.toResponse(responsavel);
    }
}
