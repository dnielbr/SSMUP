package com.br.ssmup.empresa.licensa.service;

import com.br.ssmup.empresa.cadastro.dto.EmpresaResponseDto;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.cadastro.mapper.EmpresaMapper;
import com.br.ssmup.empresa.cadastro.repository.EmpresaRepository;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaCadastroDto;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaResponseDto;
import com.br.ssmup.empresa.licensa.entity.LicensaSanitaria;
import com.br.ssmup.empresa.licensa.enums.StatusLicensa;
import com.br.ssmup.empresa.licensa.mapper.LicensaSanitariaMapper;
import com.br.ssmup.empresa.licensa.repository.LicensaSanitariaRepository;
import com.br.ssmup.core.exception.BusinessRuleException;
import com.br.ssmup.core.exception.DuplicateResourceException;
import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.pdf.service.GeradorPdfService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LicensaSanitariaService {

    private final LicensaSanitariaRepository licensaSanitariaRepository;
    private final LicensaSanitariaMapper licensaSanitariaMapper;
    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;
    private final GeradorPdfService geradorPdfService;

    public List<LicensaSanitariaResponseDto> buscarLicensasSanitaria() {
        return licensaSanitariaRepository.findAll().stream()
                .map(licensaSanitariaMapper::toResponse)
                .toList();
    }

    public Page<LicensaSanitariaResponseDto> buscarLicensasSanitariaPagable(Pageable pageable) {
        return licensaSanitariaRepository.findAll(pageable).map(licensaSanitariaMapper::toResponse);
    }

    public Page<LicensaSanitariaResponseDto> buscarLicensasPageableFilter(Specification<LicensaSanitaria> spec, Pageable pageable) {
        return licensaSanitariaRepository.findAll(spec, pageable).map(licensaSanitariaMapper::toResponse);
    }

    public LicensaSanitariaResponseDto buscarLicencaSanitariaById(Long id) {
        LicensaSanitaria entity = licensaSanitariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Licença sanitária não encontrada"));
        return licensaSanitariaMapper.toResponse(entity);
    }

    public LicensaSanitariaResponseDto buscarLicencaSanitariaByNumControle(String numControle) {
        LicensaSanitaria entity = licensaSanitariaRepository.findByNumControle(numControle)
                .orElseThrow(() -> new ResourceNotFoundException("Licença sanitária não encontrada"));
        return licensaSanitariaMapper.toResponse(entity);
    }

    public List<LicensaSanitariaResponseDto> listarLicensasSanitariasByEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        return empresa.getLicensasSanitarias().stream()
                .map(licensaSanitariaMapper::toResponse)
                .toList();
    }

    @CacheEvict(cacheNames = "empresas", key = "#empresaId")
    @Transactional
    public LicensaSanitariaResponseDto saveLicensaSanitariaByEmpresa(Long empresaId, LicensaSanitariaCadastroDto dto) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        LicensaSanitaria licensaSanitaria = licensaSanitariaMapper.toEntity(dto);
        licensaSanitaria.setEmpresa(empresa);

        return licensaSanitariaMapper.toResponse(licensaSanitariaRepository.save(licensaSanitaria));
    }

    @Transactional
    public byte[] emitirAlvara(Long idEmpresa, String numControle) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        if (!empresa.isAtivo()) {
            throw new BusinessRuleException("Não é possível emitir licença para empresa inativa.");
        }

        if (licensaSanitariaRepository.existsByEmpresaIdAndStatus(idEmpresa, StatusLicensa.ATIVA)) {
            throw new BusinessRuleException("Empresa já possui uma licença sanitária ativa.");
        }

        if (licensaSanitariaRepository.findByNumControle(numControle).isPresent()) {
            throw new DuplicateResourceException("Número de controle '" + numControle + "' já está em uso.");
        }

        LicensaSanitaria novaLicensa = new LicensaSanitaria();
        novaLicensa.setEmpresa(empresa);
        novaLicensa.setNumControle(numControle);
        novaLicensa.setStatus(StatusLicensa.ATIVA);

        LicensaSanitaria licensaSalva = licensaSanitariaRepository.save(novaLicensa);

        EmpresaResponseDto empresaDto = empresaMapper.toResponse(empresa);
        LicensaSanitariaResponseDto licensaDto = licensaSanitariaMapper.toResponse(licensaSalva);

        return geradorPdfService.gerarLicensaSanitariaPdf(empresaDto, licensaDto);
    }

    @Transactional
    public byte[] reemitirLicensa(Long idEmpresa, String novoNumControle, String motivoCancelamento) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        if (!empresa.isAtivo()) {
            throw new BusinessRuleException("Não é possível reemitir licença para empresa inativa.");
        }

        LicensaSanitaria licensaAtiva = licensaSanitariaRepository
                .findFirstByEmpresaIdAndStatus(idEmpresa, StatusLicensa.ATIVA)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma licença ativa encontrada para esta empresa."));

        if (licensaSanitariaRepository.findByNumControle(novoNumControle).isPresent()) {
            throw new DuplicateResourceException("Número de controle '" + novoNumControle + "' já está em uso.");
        }

        // Cancelar a licença antiga
        licensaAtiva.setStatus(StatusLicensa.CANCELADA);
        licensaAtiva.setMotivoCancelamento(motivoCancelamento);

        // Criar nova licença
        LicensaSanitaria novaLicensa = new LicensaSanitaria();
        novaLicensa.setEmpresa(empresa);
        novaLicensa.setNumControle(novoNumControle);
        novaLicensa.setStatus(StatusLicensa.ATIVA);

        LicensaSanitaria licensaSalva = licensaSanitariaRepository.save(novaLicensa);

        // Vincular a licença antiga à nova (substituta)
        licensaAtiva.setLicensaSubstitutaId(licensaSalva.getId());
        licensaSanitariaRepository.save(licensaAtiva);

        EmpresaResponseDto empresaDto = empresaMapper.toResponse(empresa);
        LicensaSanitariaResponseDto licensaDto = licensaSanitariaMapper.toResponse(licensaSalva);

        return geradorPdfService.gerarLicensaSanitariaPdf(empresaDto, licensaDto);
    }

    @Transactional
    public LicensaSanitariaResponseDto cancelarLicensa(Long idLicensa, String motivo) {
        LicensaSanitaria licensa = licensaSanitariaRepository.findById(idLicensa)
                .orElseThrow(() -> new ResourceNotFoundException("Licença sanitária não encontrada"));

        if (licensa.getStatus() != StatusLicensa.ATIVA) {
            throw new BusinessRuleException("Apenas licenças ativas podem ser canceladas.");
        }

        licensa.setStatus(StatusLicensa.CANCELADA);
        licensa.setMotivoCancelamento(motivo);

        return licensaSanitariaMapper.toResponse(licensaSanitariaRepository.save(licensa));
    }

    public byte[] imprimirLicensa(Long idEmpresa) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        LicensaSanitaria licensa = licensaSanitariaRepository
                .findFirstByEmpresaIdAndStatus(idEmpresa, StatusLicensa.ATIVA)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma licença ativa encontrada para esta empresa."));

        EmpresaResponseDto empresaDto = empresaMapper.toResponse(empresa);
        LicensaSanitariaResponseDto licensaDto = licensaSanitariaMapper.toResponse(licensa);

        return geradorPdfService.gerarLicensaSanitariaPdf(empresaDto, licensaDto);
    }
}