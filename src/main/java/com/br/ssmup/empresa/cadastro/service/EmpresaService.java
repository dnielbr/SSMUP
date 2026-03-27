package com.br.ssmup.empresa.cadastro.service;

import com.br.ssmup.empresa.cnae.entity.Cnae;
import com.br.ssmup.empresa.cnae.enums.RiscoSanitario;
import com.br.ssmup.empresa.cnae.repository.CnaeRepository;
import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.empresa.cadastro.dto.*;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.cadastro.mapper.EmpresaMapper;
import com.br.ssmup.empresa.cadastro.repository.EmpresaRepository;
import com.br.ssmup.empresa.cadastro.solr.EmpresaSolrService;
import com.br.ssmup.empresa.cadastro.specification.EmpresaSpecifications;
import com.br.ssmup.empresa.historico.dto.HistoricoSituacaoRequestDto;
import com.br.ssmup.empresa.historico.enums.TipoSituacao;
import com.br.ssmup.empresa.historico.service.HistoricoSituacaoService;
import com.br.ssmup.empresa.responsavel.entity.Responsavel;
import com.br.ssmup.empresa.responsavel.repository.ResponsavelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import static com.br.ssmup.core.util.CacheNames.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final EmpresaMapper empresaMapper;
//    private final CnaeRepository cnaeRepository;
    private final EmpresaSolrService empresaSolrService;
    private final HistoricoSituacaoService historicoSituacaoService;

    @Caching(put = {@CachePut(cacheNames = EMPRESAS, key = "#result.id")})
    @Transactional
    public EmpresaResponseDto saveEmpresa(EmpresaCadastroDto dto) {
        Empresa empresa = empresaMapper.toEntity(dto);

        // Normalizar email vazio para null (evita conflito de unique constraint)
        if (empresa.getEmail() != null && empresa.getEmail().isBlank()) {
            empresa.setEmail(null);
        }

        if (empresa.getInscricaoEstadual() != null && empresa.getInscricaoEstadual().isBlank()) {
            empresa.setInscricaoEstadual(null);
        }

//        if (dto.cnaeCodigo() != null) {
//            Cnae cnae = cnaeRepository.findByCodigo(dto.cnaeCodigo())
//                    .orElseThrow(() -> new RuntimeException("CNAE não encontrado: " + dto.cnaeCodigo()));
//            empresa.setCnaePrincipal(cnae);
//        }

        Responsavel responsavel = responsavelRepository.findByCpf(empresa.getResponsavel().getCpf()).orElse(null);
        if (responsavel == null) {
            responsavelRepository.save(empresa.getResponsavel());
        } else {
            empresa.setResponsavel(responsavel);
        }

        Empresa responseEmpresa = empresaRepository.save(empresa);
        log.info("Empresa salva com sucesso!");
        empresaSolrService.salvarNoSolr(responseEmpresa);
        return empresaMapper.toResponse(responseEmpresa);
    }

    public List<EmpresaResponseDto> listarEmpresas() {
        return empresaRepository.findAll().stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

//    @Cacheable(cacheNames = EMPRESAS_PAGEABLE)
    public Page<EmpresaResponseDto> listarEmpresasPageable(Pageable pageable) {
        log.info("Iniciando busca na lista paginada de empresas.");
        return empresaRepository.findAll(pageable).map(empresaMapper::toResponse);
    }

    public List<EmpresaResponseDto> listarEmpresasFilter(EmpresaFilterDto filter) {
        Specification<Empresa> spec = EmpresaSpecifications.buildSpecification(filter);
        return empresaRepository.findAll(spec).stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

//    @Cacheable(cacheNames = EMPRESAS_PAGEABLE_FILTER)
    public Page<EmpresaResponseDto> listarEmpresasPageableFilter(EmpresaFilterDto filter, Pageable pageable) {
        log.info("Iniciando busca paginada de empresas com filtros: {}", filter);
        Specification<Empresa> spec = EmpresaSpecifications.buildSpecification(filter);
        return empresaRepository.findAll(spec, pageable).map(empresaMapper::toResponse);
    }

    public List<EmpresaResponseDto> listarEmpresasAtivas() {
        return empresaRepository.findByAtivo(true).stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

    public List<EmpresaResponseDto> listarEmpresasInativas() {
        return empresaRepository.findByAtivo(false).stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

    @Cacheable(cacheNames = EMPRESAS, key = "#id")
    public EmpresaResponseDto getEmpresaById(Long id) {
        log.info("Buscando empresa com id: {}", id);
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> {
            log.error("Empresa não encontrada com id: {}", id);
            return new ResourceNotFoundException("Empresa não encontrada");
        });
        return empresaMapper.toResponse(empresa);
    }

    @Caching(evict = {@CacheEvict(cacheNames = EMPRESAS, key = "#id")})
    @Transactional
    public void deleteByIdEmpresa(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa não encontrada");
        }
        empresaRepository.deleteById(id);
        empresaSolrService.removerDoSolr(id);
    }

    @Caching(evict = {@CacheEvict(cacheNames = EMPRESAS, key = "#id")})
    @Transactional
    public void inativarEmpresa(Long id, HistoricoSituacaoRequestDto motivo) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        empresa.setAtivo(false);
        empresaRepository.save(empresa);
        historicoSituacaoService.gravarHistoricoSituacao(motivo.motivo(), empresa, TipoSituacao.INATIVACAO);
    }

    @Caching(evict = {@CacheEvict(cacheNames = EMPRESAS, key = "#id")})
    @Transactional
    public void ativarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        empresa.setAtivo(true);
        empresaRepository.save(empresa);
    }

    @Caching(evict = {@CacheEvict(cacheNames = EMPRESAS, key = "#id")})
    @Transactional
    public EmpresaAtualizarDto atualizarEmpresa(Long id, EmpresaAtualizarDto dto) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        empresaMapper.updateFromDto(dto, empresa);

        Empresa empresaSalva = empresaRepository.save(empresa);
        log.info("Empresa atualizada com sucesso");
        empresaSolrService.salvarNoSolr(empresaSalva);
        return empresaMapper.toUpdate(empresaSalva);
    }

//    public EmpresaRiscoResponseDto buscarQtEmpresasRisco() {
//        long qtBaixo = empresaRepository.countByCnaePrincipalRisco(RiscoSanitario.RISCO_I_BAIXO);
//        long qtMedio = empresaRepository.countByCnaePrincipalRisco(RiscoSanitario.RISCO_II_MEDIO);
//        long qtAlto = empresaRepository.countByCnaePrincipalRisco(RiscoSanitario.RISCO_III_ALTO);
//        return new EmpresaRiscoResponseDto(qtBaixo, qtMedio, qtAlto);
//    }

    public Page<EmpresaResponseDto> buscarEmpresasPorText(String termo, Pageable pageable) {
        log.info("Iniciando busca aproximada pelo termo: {}", termo);
        Page<EmpresaSolrDto> pageSolr = empresaSolrService.buscarAproximada(termo, pageable);
        if (pageSolr.isEmpty()) return Page.empty(pageable);

        List<Long> ids = pageSolr.getContent().stream()
                .map(doc -> Long.valueOf(doc.getId()))
                .toList();

        List<Empresa> empresasDesordenadas = empresaRepository.findAllById(ids);
        Map<Long, Empresa> empresaMap = empresasDesordenadas.stream()
                .collect(Collectors.toMap(Empresa::getId, e -> e));

        List<EmpresaResponseDto> empresasOrdenadas = ids.stream()
                .map(empresaMap::get)
                .filter(Objects::nonNull)
                .map(empresaMapper::toResponse)
                .toList();

        return new PageImpl<>(empresasOrdenadas, pageable, pageSolr.getTotalElements());
    }

    public void sincronizarBaseComSolr() {
        log.info("Extraindo dados do PostgreSQL para o Apache Solr...");
        List<Empresa> todasEmpresas = empresaRepository.findAll();
        List<EmpresaSolrDto> documentos = todasEmpresas.stream()
                .map(emp -> new EmpresaSolrDto(emp.getId().toString(), emp.getRazaoSocial(), emp.getNomeFantasia()))
                .toList();
        empresaSolrService.sincronizarEmLote(documentos);
    }

    public List<EmpresaCadastroMensalDto> listarCadastrosMensais(int ano) {
        List<Object[]> resultados = empresaRepository.contarCadastrosPorMes(ano);

        Map<Integer, Long> mapaMensal = resultados.stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).intValue(),
                        r -> ((Number) r[1]).longValue()
                ));

        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(mes -> new EmpresaCadastroMensalDto(mes, mapaMensal.getOrDefault(mes, 0L)))
                .toList();
    }
}
