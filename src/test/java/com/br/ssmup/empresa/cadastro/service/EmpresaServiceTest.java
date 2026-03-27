package com.br.ssmup.empresa.cadastro.service;

import com.br.ssmup.empresa.cnae.enums.RiscoSanitario;
import com.br.ssmup.empresa.cnae.repository.CnaeRepository;
import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.empresa.cadastro.dto.*;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.cadastro.mapper.EmpresaMapper;
import com.br.ssmup.empresa.cadastro.repository.EmpresaRepository;
import com.br.ssmup.empresa.cadastro.solr.EmpresaSolrService;
import com.br.ssmup.empresa.historico.dto.HistoricoSituacaoRequestDto;
import com.br.ssmup.empresa.historico.service.HistoricoSituacaoService;
import com.br.ssmup.empresa.responsavel.entity.Responsavel;
import com.br.ssmup.empresa.responsavel.repository.ResponsavelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock private EmpresaRepository empresaRepository;
    @Mock private ResponsavelRepository responsavelRepository;
    @Mock private EmpresaMapper empresaMapper;
    @Mock private CnaeRepository cnaeRepository;
    @Mock private EmpresaSolrService empresaSolrService;
    @Mock private HistoricoSituacaoService historicoSituacaoService;

    @InjectMocks
    private EmpresaService empresaService;

    private Empresa criarEmpresaFake() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setRazaoSocial("Empresa Teste LTDA");
        empresa.setNomeFantasia("Teste");
        empresa.setCnpj("12345678000199");
        empresa.setAtivo(true);
        return empresa;
    }

    @Test
    @DisplayName("Deve salvar empresa com sucesso")
    void deveSalvarEmpresaComSucesso() {
        Empresa empresa = criarEmpresaFake();
        Responsavel resp = new Responsavel();
        resp.setCpf("12345678901");
        empresa.setResponsavel(resp);

        EmpresaCadastroDto cadastroDto = mock(EmpresaCadastroDto.class);
        EmpresaResponseDto responseDto = mock(EmpresaResponseDto.class);

        when(empresaMapper.toEntity(cadastroDto)).thenReturn(empresa);
        when(responsavelRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);
        when(empresaMapper.toResponse(empresa)).thenReturn(responseDto);

        EmpresaResponseDto result = empresaService.saveEmpresa(cadastroDto);

        assertThat(result).isNotNull();
        verify(empresaRepository).save(any(Empresa.class));
        verify(responsavelRepository).save(resp);
        verify(empresaSolrService).salvarNoSolr(empresa);
    }

    @Test
    @DisplayName("Deve listar todas as empresas")
    void deveListarEmpresas() {
        List<Empresa> empresas = List.of(criarEmpresaFake());
        EmpresaResponseDto responseDto = mock(EmpresaResponseDto.class);

        when(empresaRepository.findAll()).thenReturn(empresas);
        when(empresaMapper.toResponse(any(Empresa.class))).thenReturn(responseDto);

        List<EmpresaResponseDto> result = empresaService.listarEmpresas();

        assertThat(result).hasSize(1);
        verify(empresaRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar empresas paginadas")
    void deveListarEmpresasPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Empresa> page = new PageImpl<>(List.of(criarEmpresaFake()));
        EmpresaResponseDto responseDto = mock(EmpresaResponseDto.class);

        when(empresaRepository.findAll(pageable)).thenReturn(page);
        when(empresaMapper.toResponse(any(Empresa.class))).thenReturn(responseDto);

        Page<EmpresaResponseDto> result = empresaService.listarEmpresasPageable(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar empresa por ID")
    void deveBuscarEmpresaPorId() {
        Empresa empresa = criarEmpresaFake();
        EmpresaResponseDto responseDto = mock(EmpresaResponseDto.class);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaMapper.toResponse(empresa)).thenReturn(responseDto);

        EmpresaResponseDto result = empresaService.getEmpresaById(1L);

        assertThat(result).isNotNull();
        verify(empresaRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando empresa não encontrada")
    void deveLancarExcecaoQuandoEmpresaNaoEncontrada() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaService.getEmpresaById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve deletar empresa por ID")
    void deveDeletarEmpresa() {
        when(empresaRepository.existsById(1L)).thenReturn(true);

        empresaService.deleteByIdEmpresa(1L);

        verify(empresaRepository).deleteById(1L);
        verify(empresaSolrService).removerDoSolr(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar empresa inexistente")
    void deveLancarExcecaoAoDeletarEmpresaInexistente() {
        when(empresaRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> empresaService.deleteByIdEmpresa(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve inativar empresa")
    void deveInativarEmpresa() {
        Empresa empresa = criarEmpresaFake();
        HistoricoSituacaoRequestDto motivo = new HistoricoSituacaoRequestDto("Fechamento temporário");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        empresaService.inativarEmpresa(1L, motivo);

        assertThat(empresa.isAtivo()).isFalse();
        verify(empresaRepository).save(empresa);
        verify(historicoSituacaoService).gravarHistoricoSituacao(eq("Fechamento temporário"), eq(empresa), any());
    }

    @Test
    @DisplayName("Deve ativar empresa")
    void deveAtivarEmpresa() {
        Empresa empresa = criarEmpresaFake();
        empresa.setAtivo(false);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        empresaService.ativarEmpresa(1L);

        assertThat(empresa.isAtivo()).isTrue();
        verify(empresaRepository).save(empresa);
    }

    @Test
    @DisplayName("Deve atualizar empresa usando mapper")
    void deveAtualizarEmpresa() {
        Empresa empresa = criarEmpresaFake();
        EmpresaAtualizarDto dto = mock(EmpresaAtualizarDto.class);
        EmpresaAtualizarDto responseDto = mock(EmpresaAtualizarDto.class);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(empresa)).thenReturn(empresa);
        when(empresaMapper.toUpdate(empresa)).thenReturn(responseDto);

        EmpresaAtualizarDto result = empresaService.atualizarEmpresa(1L, dto);

        assertThat(result).isNotNull();
        verify(empresaMapper).updateFromDto(dto, empresa);
        verify(empresaSolrService).salvarNoSolr(empresa);
    }

    @Test
    @DisplayName("Deve listar empresas ativas usando repository query")
    void deveListarEmpresasAtivas() {
        List<Empresa> ativas = List.of(criarEmpresaFake());
        EmpresaResponseDto responseDto = mock(EmpresaResponseDto.class);

        when(empresaRepository.findByAtivo(true)).thenReturn(ativas);
        when(empresaMapper.toResponse(any(Empresa.class))).thenReturn(responseDto);

        List<EmpresaResponseDto> result = empresaService.listarEmpresasAtivas();

        assertThat(result).hasSize(1);
        verify(empresaRepository).findByAtivo(true);
    }

//    @Test
//    @DisplayName("Deve buscar contagem de empresas por risco")
//    void deveBuscarQtEmpresasRisco() {
//        when(empresaRepository.countByCnaePrincipalRisco(RiscoSanitario.RISCO_I_BAIXO)).thenReturn(5L);
//        when(empresaRepository.countByCnaePrincipalRisco(RiscoSanitario.RISCO_II_MEDIO)).thenReturn(3L);
//        when(empresaRepository.countByCnaePrincipalRisco(RiscoSanitario.RISCO_III_ALTO)).thenReturn(2L);
//
//        EmpresaRiscoResponseDto result = empresaService.buscarQtEmpresasRisco();
//
//        assertThat(result.qtEmpresasBaixoRisco()).isEqualTo(5);
//        assertThat(result.qtEmpresasRiscoMedio()).isEqualTo(3);
//        assertThat(result.qtEmpresasRiscoAlto()).isEqualTo(2);
//    }

    @Test
    @DisplayName("Deve listar cadastros mensais retornando 12 meses")
    void deveListarCadastrosMensais() {
        // Simula retorno do repositório: apenas meses 1 e 3 têm cadastros
        List<Object[]> dados = List.of(
                new Object[]{1, 5L},
                new Object[]{3, 2L}
        );

        when(empresaRepository.contarCadastrosPorMes(2026)).thenReturn(dados);

        List<EmpresaCadastroMensalDto> result = empresaService.listarCadastrosMensais(2026);

        assertThat(result).hasSize(12);
        assertThat(result.get(0).quantidade()).isEqualTo(5); // Jan
        assertThat(result.get(1).quantidade()).isEqualTo(0); // Fev (sem dados)
        assertThat(result.get(2).quantidade()).isEqualTo(2); // Mar
        assertThat(result.get(3).quantidade()).isEqualTo(0); // Abr (sem dados)
    }

    @Test
    @DisplayName("Deve normalizar email vazio para null ao salvar empresa")
    void deveNormalizarEmailVazioParaNull() {
        Empresa empresa = criarEmpresaFake();
        empresa.setEmail("");
        Responsavel resp = new Responsavel();
        resp.setCpf("12345678901");
        empresa.setResponsavel(resp);

        EmpresaCadastroDto cadastroDto = mock(EmpresaCadastroDto.class);
        EmpresaResponseDto responseDto = mock(EmpresaResponseDto.class);

        when(empresaMapper.toEntity(cadastroDto)).thenReturn(empresa);
        when(responsavelRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);
        when(empresaMapper.toResponse(empresa)).thenReturn(responseDto);

        empresaService.saveEmpresa(cadastroDto);

        assertThat(empresa.getEmail()).isNull();
        verify(empresaRepository).save(argThat(e -> e.getEmail() == null));
    }

    @Test
    @DisplayName("Deve manter email válido ao salvar empresa")
    void deveManterEmailValidoAoSalvar() {
        Empresa empresa = criarEmpresaFake();
        empresa.setEmail("test@example.com");
        Responsavel resp = new Responsavel();
        resp.setCpf("12345678901");
        empresa.setResponsavel(resp);

        EmpresaCadastroDto cadastroDto = mock(EmpresaCadastroDto.class);
        EmpresaResponseDto responseDto = mock(EmpresaResponseDto.class);

        when(empresaMapper.toEntity(cadastroDto)).thenReturn(empresa);
        when(responsavelRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);
        when(empresaMapper.toResponse(empresa)).thenReturn(responseDto);

        empresaService.saveEmpresa(cadastroDto);

        assertThat(empresa.getEmail()).isEqualTo("test@example.com");
    }
}

