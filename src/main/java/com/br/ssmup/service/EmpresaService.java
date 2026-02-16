package com.br.ssmup.service;

import com.br.ssmup.dto.*;
import com.br.ssmup.entities.*;
import com.br.ssmup.enums.RiscoSanitario;
import com.br.ssmup.enums.TipoSituacao;
import com.br.ssmup.exceptions.AuthenticationException;
import com.br.ssmup.exceptions.ResourceNotFoundException;
import com.br.ssmup.mapper.*;
import com.br.ssmup.repository.*;
import com.br.ssmup.specifications.EmpresaSpecifications;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final LicensaSanitariaRepository licensaSanitariaRepository;
    private final EmpresaMapper empresaMapper;
    private final EnderecoMapper  enderecoMapper;
    private final ResponsavelMapper responsavelMapper;
    private final LicensaSanitariaMapper licensaMapper;
    private final CnaeRepository  cnaeRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoSitucaoRepository  historicoSitucaoRepository;
    private final HistoricoSituacaoMapper historicoSituacaoMapper;


    public EmpresaService(EmpresaRepository empresaRepository, ResponsavelRepository responsavelRepository, LicensaSanitariaRepository licensaSanitariaRepository, EmpresaMapper empresaMapper, EnderecoMapper enderecoMapper, ResponsavelMapper responsavelMapper, LicensaSanitariaMapper licensaMapper, CnaeRepository cnaeRepository, UsuarioRepository usuarioRepository, HistoricoSitucaoRepository historicoSitucaoRepository, HistoricoSituacaoMapper historicoSituacaoMapper) {
        this.empresaRepository = empresaRepository;
        this.responsavelRepository = responsavelRepository;
        this.licensaSanitariaRepository = licensaSanitariaRepository;
        this.empresaMapper = empresaMapper;
        this.enderecoMapper = enderecoMapper;
        this.responsavelMapper = responsavelMapper;
        this.licensaMapper = licensaMapper;
        this.cnaeRepository = cnaeRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoSitucaoRepository = historicoSitucaoRepository;
        this.historicoSituacaoMapper = historicoSituacaoMapper;
    }

    @Transactional
    public EmpresaResponseDto saveEmpresa(EmpresaCadastroDto dto) {
        Empresa empresa = empresaMapper.toEntity(dto);

        if(dto.cnaeCodigo() != null){
            Cnae cnae = cnaeRepository.findByCodigo(dto.cnaeCodigo()).orElseThrow(() -> new RuntimeException("CNAE não encontrado: " + dto.cnaeCodigo()));
            System.out.println(cnae.getRisco());
            empresa.setCnaePrincipal(cnae);
        }

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

    //Retirar SPECIFICATIONS
    public List<EmpresaResponseDto> listarEmpresasFilter(EmpresaFilterDto filter) {

        Specification<Empresa> spec = EmpresaSpecifications.buildSpecification(filter);

        return empresaRepository.findAll(spec).stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

    //Retirar SPECIFICATIONS
    public Page<EmpresaResponseDto> listarEmpresasPageableFilter(EmpresaFilterDto filter, Pageable pageable) {
        log.info("Iniciando busca paginada de empresas com filtros: {}", filter);
        Specification<Empresa> spec = EmpresaSpecifications.buildSpecification(filter);
        return empresaRepository.findAll(spec, pageable).map(empresaMapper::toResponse);
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
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
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
    public void inativarEmpresa(Long id, HistoricoSituacaoRequestDto motivo) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        empresa.setAtivo(false);
        empresaRepository.save(empresa);
        gravarHistoricoSituacao(motivo.motivo(), empresa, TipoSituacao.INATIVACAO);
    }

    @Transactional
    public void ativarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() ->  new ResourceNotFoundException("Empresa não encontrada"));
        empresa.setAtivo(true);
        empresaRepository.save(empresa);
    }

//    @Transactional
//    public void realizarInspecao(Long id) {
//        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
//        empresa.setInspecao(true);
//        empresaRepository.save(empresa);
//    }

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

    public void gravarHistoricoSituacao(String motivo, Empresa empresa, TipoSituacao tipoSituacao) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new AuthenticationException("Nenhum usuario autenticado encontrado");
        }

        Usuario usuario =  usuarioRepository.findByEmail((String) authentication.getPrincipal()).orElseThrow(()-> new ResourceNotFoundException("Usuario do token nao existe no banco"));

        HistoricoSituacao historicoSituacao = new HistoricoSituacao();
        historicoSituacao.setUsuario(usuario);
        historicoSituacao.setEmpresa(empresa);
        historicoSituacao.setMotivo(motivo);
        historicoSituacao.setTipoSituacao(tipoSituacao);
        historicoSitucaoRepository.save(historicoSituacao);
    }

    public List<HistoricoSituacaoResponseDto> listarHistoricoSituacao(Long id) {
        List<HistoricoSituacao> historicoSituacaoList = historicoSitucaoRepository.findByEmpresaIdOrderByDataDesc(id);

        return historicoSituacaoList.stream()
                .map(historicoSituacaoMapper::toDto)
                .toList();
    }

    public EmpresaRiscoResponseDto buscarQtEmpresasRisco(){
        long qtBaixo = empresaRepository.countByCnaePrincipalRisco(RiscoSanitario.RISCO_I_BAIXO);
        long qtMedio = empresaRepository.countByCnaePrincipalRisco(RiscoSanitario.RISCO_II_MEDIO);
        long qtAlto = empresaRepository.countByCnaePrincipalRisco(RiscoSanitario.RISCO_III_ALTO);
        return new EmpresaRiscoResponseDto(qtBaixo, qtMedio, qtAlto);
    }

}
