package com.br.ssmup.service;

import com.br.ssmup.dto.*;
import com.br.ssmup.entities.Empresa;
import com.br.ssmup.entities.Endereco;
import com.br.ssmup.entities.LicensaSanitaria;
import com.br.ssmup.entities.Responsavel;
import com.br.ssmup.exceptions.ResourceNotFoundException;
import com.br.ssmup.mapper.EmpresaMapper;
import com.br.ssmup.mapper.EnderecoMapper;
import com.br.ssmup.mapper.LicensaSanitariaMapper;
import com.br.ssmup.mapper.ResponsavelMapper;
import com.br.ssmup.repository.EmpresaRepository;
import com.br.ssmup.repository.LicensaSanitariaRepository;
import com.br.ssmup.repository.ResponsavelRepository;
import com.br.ssmup.specifications.EmpresaSpecifications;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final LicensaSanitariaRepository licensaSanitariaRepository;
    private final EmpresaMapper empresaMapper;
    private final EnderecoMapper  enderecoMapper;
    private final ResponsavelMapper responsavelMapper;
    private final LicensaSanitariaMapper licensaMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;


    public EmpresaService(EmpresaRepository empresaRepository, ResponsavelRepository responsavelRepository, LicensaSanitariaRepository licensaSanitariaRepository, EmpresaMapper empresaMapper, EnderecoMapper enderecoMapper, ResponsavelMapper responsavelMapper, LicensaSanitariaMapper licensaMapper, RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.empresaRepository = empresaRepository;
        this.responsavelRepository = responsavelRepository;
        this.licensaSanitariaRepository = licensaSanitariaRepository;
        this.empresaMapper = empresaMapper;
        this.enderecoMapper = enderecoMapper;
        this.responsavelMapper = responsavelMapper;
        this.licensaMapper = licensaMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EmpresaResponseDto saveEmpresa(EmpresaCadastroDto dto) {
        Empresa empresa = empresaMapper.toEntity(dto);
        Responsavel responsavel = responsavelRepository.findByCpf(empresa.getResponsavel().getCpf()).orElse(null);
        if(responsavel == null){
            responsavelRepository.save(empresa.getResponsavel());
            return empresaMapper.toResponse(empresaRepository.save(empresa));
        }
        empresa.setResponsavel(responsavel);
        return empresaMapper.toResponse(empresaRepository.save(empresa));
    }

    @Transactional
    public LicensaSanitariaResponseDto saveLicensaSanitaria(Long id, LicensaSanitariaCadastroDto dto) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(()-> new  ResourceNotFoundException("Empresa não encontrada"));
        LicensaSanitaria licensaSanitaria = licensaMapper.toEntity(dto);
        licensaSanitaria.setEmpresa(empresa);
        return licensaMapper.toResponse(licensaSanitariaRepository.save(licensaSanitaria));
    }

    public List<EmpresaResponseDto>  listarEmpresas() {
        return empresaRepository.findAll().stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

    public Page<EmpresaResponseDto> listarEmpresasPageable(Pageable pageable) {
        return empresaRepository.findAll(pageable).map(empresaMapper::toResponse);
    }

    public List<EmpresaResponseDto> listarEmpresasFilter(EmpresaFilterDto filter) {

        Specification<Empresa> spec = EmpresaSpecifications.buildSpecification(filter);

        return empresaRepository.findAll(spec).stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

    public Page<EmpresaResponseDto> listarEmpresasPageableFilter(EmpresaFilterDto filter, Pageable pageable) {
        //Chave que vai ser armazenada no Redis
        String key = "empresas::paged_filter::"
                + filter.hashCode() + "::"
                + pageable.getPageNumber() + "::"
                + pageable.getPageSize() + "::"
                + pageable.getSort().toString().replace(":", "_");

        Object cache = redisTemplate.opsForValue().get(key);

        if (cache != null) {
            //Criando um tipo generico para evitar problemas com cast
            JavaType type = objectMapper.getTypeFactory().constructParametricType(
                    Page.class, EmpresaResponseDto.class
            );

            try {
                return objectMapper.convertValue(cache, type);
            } catch (IllegalArgumentException e) {
                redisTemplate.delete(key);
            }
        }

        Specification<Empresa> spec = EmpresaSpecifications.buildSpecification(filter);
        Page<EmpresaResponseDto> response = empresaRepository.findAll(spec, pageable).map(empresaMapper::toResponse);
        redisTemplate.opsForValue().set(key, response);
        return response;
    }


    public List<EmpresaResponseDto>  listarEmpresasAtivas() {
        return empresaRepository.findAll().stream()
                .filter(Empresa::isAtivo)
                .map(empresaMapper::toResponse)
                .toList();
    }

    public List<EmpresaResponseDto>  listarEmpresasInativas() {
        return empresaRepository.findAll().stream()
                .filter((empresa) -> !empresa.isAtivo())
                .map(empresaMapper::toResponse)
                .toList();
    }

    public List<LicensaSanitariaResponseDto> listarLicensasSanitarias(Long id) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(()-> new  ResourceNotFoundException("Empresa não encontrada"));
        return empresa.getLicensasSanitarias().stream()
                .map(licensaMapper::toResponse)
                .toList();
    }


    public EmpresaResponseDto getEmpresaById(Long id) {
        Object cache = redisTemplate.opsForValue().get("EMPRESA_DATA:" + id);
        if(cache != null){
            return objectMapper.convertValue(cache, EmpresaResponseDto.class);
        }
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        redisTemplate.opsForValue().set("EMPRESA_DATA:" + id, empresaMapper.toResponse(empresa));
        return empresaMapper.toResponse(empresa);
    }

    @Transactional
    public void  deleteByIdEmpresa(Long id) {
        if(!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa não encontrada");
        }
        empresaRepository.deleteById(id);
    }

    @Transactional
    public void inativarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        empresa.setAtivo(false);
        empresaRepository.save(empresa);
    }

    @Transactional
    public EmpresaAtualizarDto atualizarEmpresa(Long id, EmpresaAtualizarDto dto) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        if(dto.razaoSocial() != null && !dto.razaoSocial().isBlank()) {
            empresa.setRazaoSocial(dto.razaoSocial());
        }

        if(dto.nomeFantasia()  != null &&  !dto.nomeFantasia().isBlank()) {
            empresa.setNomeFantasia(dto.nomeFantasia());
        }

        if (dto.cnpj() != null && !dto.cnpj().isBlank()) {
            empresa.setCnpj(dto.cnpj());
        }

        if(dto.inscricaoEstadual() != null &&  !dto.inscricaoEstadual().isBlank()) {
            empresa.setInscricaoEstadual(dto.inscricaoEstadual());
        }

        if(dto.atividadeFirma() != null &&   !dto.atividadeFirma().isBlank()) {
            empresa.setAtividadeFirma(dto.atividadeFirma());
        }

        if(dto.subAtividade() != null &&   !dto.subAtividade().isBlank()) {
            empresa.setSubAtividade(dto.subAtividade());
        }

        if(dto.dataInicioFuncionamento() != null) {
            empresa.setDataInicioFuncionamento(dto.dataInicioFuncionamento());
        }

        return empresaMapper.toUpdate( empresaRepository.save(empresa));
    }

    @Transactional
    public EnderecoResponseDto atualizarEndereco(Long id, EnderecoAtualizarDto dto) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Empresa nao encontrada"));
        Endereco endereco = empresa.getEndereco();

        if(dto.rua() != null && !dto.rua().isBlank()) {
            endereco.setRua(dto.rua());
        }

        if(dto.numero() != null && !dto.numero().isBlank()) {
            endereco.setNumero(dto.numero());
        }

        if(dto.bairro() != null && !dto.bairro().isBlank()) {
            endereco.setBairro(dto.bairro());
        }

        if(dto.cep() != null && !dto.cep().isBlank()) {
            endereco.setCep(dto.cep());
        }

        if(dto.municipio() != null && !dto.municipio().isBlank()) {
            endereco.setMunicipio(dto.municipio());
        }

        if(dto.uf() != null){
            endereco.setUf(dto.uf());
        }

        if(dto.telefone() != null && !dto.telefone().isBlank()) {
            endereco.setTelefone(dto.telefone());
        }

        empresaRepository.save(empresa);
        return enderecoMapper.toResponse(empresa.getEndereco());
    }

    @Transactional
    public ResponsavelResponseDto atualizarResponsavel(Long id, ResponsavelAtualizarDto dto) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Empresa nao encontrada"));
        Responsavel responsavel = empresa.getResponsavel();

        if(dto.nome() != null && !dto.nome().isBlank()) {
            responsavel.setNome(dto.nome());
        }

        if(dto.cpf() != null && !dto.cpf().isBlank()) {
            responsavel.setCpf(dto.cpf());
        }

        if(dto.rg() != null && !dto.rg().isBlank()) {
            responsavel.setRg(dto.rg());
        }

        if(dto.escolaridade() != null && !dto.escolaridade().isBlank()) {
            responsavel.setEscolaridade(dto.escolaridade());
        }

        if(dto.formacao() != null && !dto.formacao().isBlank()) {
            responsavel.setFormacao(dto.formacao());
        }

        if(dto.especializacao() != null && !dto.especializacao().isBlank()) {
            responsavel.setEspecializacao(dto.especializacao());
        }

        if(dto.registroConselho() != null && !dto.registroConselho().isBlank()) {
            responsavel.setRegistroConselho(dto.registroConselho());
        }

        empresaRepository.save(empresa);
        return responsavelMapper.toResponse(responsavel);
    }


}
