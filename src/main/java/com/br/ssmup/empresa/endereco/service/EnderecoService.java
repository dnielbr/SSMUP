package com.br.ssmup.empresa.endereco.service;

import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.cadastro.repository.EmpresaRepository;
import com.br.ssmup.empresa.endereco.dto.EnderecoAtualizarDto;
import com.br.ssmup.empresa.endereco.dto.EnderecoResponseDto;
import com.br.ssmup.empresa.endereco.entity.Endereco;
import com.br.ssmup.empresa.endereco.mapper.EnderecoMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import static com.br.ssmup.core.util.CacheNames.*;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EmpresaRepository empresaRepository;
    private final EnderecoMapper enderecoMapper;

    @Caching(evict = {
            @CacheEvict(cacheNames = EMPRESAS, key = "#empresaId"),
    })
    @Transactional
    public EnderecoResponseDto atualizarEndereco(Long empresaId, EnderecoAtualizarDto dto) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
        Endereco endereco = empresa.getEndereco();

        enderecoMapper.updateFromDto(dto, endereco);

        empresaRepository.save(empresa);
        return enderecoMapper.toResponse(empresa.getEndereco());
    }
}
