package com.br.ssmup.service;

import com.br.ssmup.dto.InspecaoRelatorioRequestDto;
import com.br.ssmup.dto.InspecaoRelatorioResponseDto;
import com.br.ssmup.dto.InspecaoRelatorioUpdateDto;
import com.br.ssmup.entities.Empresa;
import com.br.ssmup.entities.InspecaoRelatorio;
import com.br.ssmup.entities.Usuario;
import com.br.ssmup.enums.StatusInspecao;
import com.br.ssmup.exceptions.ResourceNotFoundException;
import com.br.ssmup.mapper.InspecaoRelatorioMapper;
import com.br.ssmup.repository.EmpresaRepository;
import com.br.ssmup.repository.InspecaoRelatorioRepository;
import com.br.ssmup.repository.UsuarioRepository;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InspecaoRelatorioService {

    private final InspecaoRelatorioRepository inspecaoRelatorioRepository;
    private final InspecaoRelatorioMapper inspecaoRelatorioMapper;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public InspecaoRelatorioService(InspecaoRelatorioRepository inspecaoRelatorioRepository, InspecaoRelatorioMapper inspecaoRelatorioMapper, EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository) {
        this.inspecaoRelatorioRepository = inspecaoRelatorioRepository;
        this.inspecaoRelatorioMapper = inspecaoRelatorioMapper;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<InspecaoRelatorioResponseDto> listarInspecaoRelatorio() {
        return inspecaoRelatorioRepository.findAll().stream()
                .map(inspecaoRelatorioMapper::toDto)
                .toList();
    }

    public List<InspecaoRelatorioResponseDto> listarInspecaoRelatorioByEmpresaId(Long empresaId) {
        empresaRepository.findById(empresaId).orElseThrow(()-> new ResourceNotFoundException("empresa nao encontrada"));
        return inspecaoRelatorioRepository.findAllByEmpresaId(empresaId).stream()
                .map(inspecaoRelatorioMapper::toDto)
                .toList();
    }

    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "empresas", key = "#inspecaoRelatorioRequestDto.empresaId()"),
                    @CacheEvict(cacheNames = "empresas_pageable", allEntries = true),
                    @CacheEvict(cacheNames = "empresas_pageableFilter", allEntries = true)
            }
    )
    @Transactional
    public InspecaoRelatorioResponseDto salvarInspecaoRelatorio(InspecaoRelatorioRequestDto inspecaoRelatorioRequestDto) {

        Empresa empresa = empresaRepository.findById(inspecaoRelatorioRequestDto.empresaId()).orElseThrow(()-> new ResourceNotFoundException("Empresa com id: " + inspecaoRelatorioRequestDto.empresaId() + " não encontrada"));

        if(inspecaoRelatorioRequestDto.statusInspecao().equals(StatusInspecao.APROVADA)){
            empresa.setInspecao(true);
            empresa = empresaRepository.save(empresa);
        }
        List<Usuario> usuarios = usuarioRepository.findAllById(inspecaoRelatorioRequestDto.usuariosId());

        if(usuarios.isEmpty()){
            throw new RuntimeException("Pelo menos um id de usuario deve ser fornecido");
        }

        InspecaoRelatorio inspecaoRelatorio = new InspecaoRelatorio();
        inspecaoRelatorio.setObjetivoInspecao(inspecaoRelatorioRequestDto.objetivoInspecao());
        inspecaoRelatorio.setObservacoes(inspecaoRelatorioRequestDto.observacoes());
        inspecaoRelatorio.setDataInspecao(inspecaoRelatorioRequestDto.dataInspecao());
        inspecaoRelatorio.setStatusInspecao(inspecaoRelatorioRequestDto.statusInspecao());
        inspecaoRelatorio.setEmpresa(empresa);
        inspecaoRelatorio.setUsuarios(usuarios);

        return inspecaoRelatorioMapper.toDto(inspecaoRelatorioRepository.save(inspecaoRelatorio));
    }

    @Transactional
    public InspecaoRelatorioResponseDto atualizarInspecaoRelatorio(Long id, InspecaoRelatorioUpdateDto dto){
        InspecaoRelatorio inspecaoRelatorio = inspecaoRelatorioRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Relatorio de inspecao nao encontrado."));

        if(dto.objetivoInspecao() != null && !dto.objetivoInspecao().isBlank()){
            inspecaoRelatorio.setObjetivoInspecao(dto.objetivoInspecao());
        }

        if(dto.observacoes() != null && !dto.observacoes().isBlank()){
            inspecaoRelatorio.setObservacoes(dto.observacoes());
        }

        if(dto.dataInspecao() != null){
            inspecaoRelatorio.setDataInspecao(dto.dataInspecao());
        }

        if(dto.statusInspecao() != null){
            inspecaoRelatorio.setStatusInspecao(dto.statusInspecao());
        }

        if(dto.usuariosId() != null && !dto.usuariosId().isEmpty()){
            List<Usuario> usuarios = usuarioRepository.findAllById(dto.usuariosId());
            if(!usuarios.isEmpty()){
                inspecaoRelatorio.setUsuarios(usuarios);
            }
        }

        return inspecaoRelatorioMapper.toDto(inspecaoRelatorioRepository.save(inspecaoRelatorio));
    }
}
