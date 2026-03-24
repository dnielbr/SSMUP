package com.br.ssmup.empresa.licensa.service;

import com.br.ssmup.empresa.cadastro.dto.EmpresaResponseDto;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.cadastro.mapper.EmpresaMapper;
import com.br.ssmup.empresa.cadastro.repository.EmpresaRepository;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaCadastroDto;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaResponseDto;
import com.br.ssmup.empresa.licensa.entity.LicensaSanitaria;
import com.br.ssmup.empresa.licensa.mapper.LicensaSanitariaMapper;
import com.br.ssmup.empresa.licensa.repository.LicensaSanitariaRepository;
import com.br.ssmup.empresa.cnae.enums.RiscoSanitario;
import com.br.ssmup.core.exception.BusinessRuleException;
import com.br.ssmup.core.exception.HighRiskInspectionException;
import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.pdf.service.GeradorPdfService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    public LicensaSanitariaResponseDto buscarLicencaSanitariaByNumControle(String numControle) {
        LicensaSanitaria entity = licensaSanitariaRepository.findByNumControle(numControle)
                .orElseThrow(() -> new RuntimeException("Licença sanitária não encontrada"));
        return licensaSanitariaMapper.toResponse(entity);
    }

    // Extracted from EmpresaService
    public List<LicensaSanitariaResponseDto> listarLicensasSanitariasByEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        return empresa.getLicensasSanitarias().stream()
                .map(licensaSanitariaMapper::toResponse)
                .toList();
    }

    // Extracted from EmpresaService
    @CacheEvict(cacheNames = "empresas", key = "#empresaId")
    @Transactional
    public LicensaSanitariaResponseDto saveLicensaSanitariaByEmpresa(Long empresaId, LicensaSanitariaCadastroDto dto) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        LicensaSanitaria licensaSanitaria = licensaSanitariaMapper.toEntity(dto);
        licensaSanitaria.setEmpresa(empresa);

        return licensaSanitariaMapper.toResponse(licensaSanitariaRepository.save(licensaSanitaria));
    }

    public byte[] emitirAlvara(Long idEmpresa) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        if (empresa.getCnaePrincipal() == null) {
            throw new BusinessRuleException("Empresa sem CNAE vinculado. Atualize o cadastro.");
        }

        RiscoSanitario riscoSanitario = empresa.getCnaePrincipal().getRisco();

        if (riscoSanitario == RiscoSanitario.RISCO_III_ALTO) {
            if (!empresa.isInspecao()) {
                throw new HighRiskInspectionException("Empresa classificada como ALTO RISCO. Necessária inspeção prévia.");
            }
        }

        LicensaSanitaria licensaParaImprimir = licensaSanitariaRepository
                .findFirstByEmpresaIdAndStatusTrue(idEmpresa)
                .orElse(null);

        if (licensaParaImprimir == null) {
            LicensaSanitaria novaLicensa = new LicensaSanitaria();
            novaLicensa.setEmpresa(empresa);
            novaLicensa.setNumControle(gerarNumeroControle());
            novaLicensa.setStatus(true);

            licensaParaImprimir = licensaSanitariaRepository.save(novaLicensa);
        }

        EmpresaResponseDto empresaDto = empresaMapper.toResponse(empresa);
        LicensaSanitariaResponseDto licensaDto = licensaSanitariaMapper.toResponse(licensaParaImprimir);

        return geradorPdfService.gerarLicensaSanitariaPdf(empresaDto, licensaDto);
    }

    private String gerarNumeroControle() {
        return LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}