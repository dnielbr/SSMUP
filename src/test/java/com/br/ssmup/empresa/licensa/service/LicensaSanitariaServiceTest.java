package com.br.ssmup.empresa.licensa.service;

import com.br.ssmup.core.exception.BusinessRuleException;
import com.br.ssmup.core.exception.DuplicateResourceException;
import com.br.ssmup.core.exception.ResourceNotFoundException;
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
import com.br.ssmup.pdf.service.GeradorPdfService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicensaSanitariaServiceTest {

    @Mock private LicensaSanitariaRepository licensaSanitariaRepository;
    @Mock private LicensaSanitariaMapper licensaSanitariaMapper;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private EmpresaMapper empresaMapper;
    @Mock private GeradorPdfService geradorPdfService;

    @InjectMocks
    private LicensaSanitariaService licensaSanitariaService;

    // ---------- buscarLicensasSanitaria ----------

    @Test
    @DisplayName("Deve listar licenças sanitárias")
    void deveListarLicensas() {
        when(licensaSanitariaRepository.findAll()).thenReturn(List.of(new LicensaSanitaria()));
        when(licensaSanitariaMapper.toResponse(any())).thenReturn(mock(LicensaSanitariaResponseDto.class));

        List<LicensaSanitariaResponseDto> result = licensaSanitariaService.buscarLicensasSanitaria();

        assertThat(result).hasSize(1);
    }

    // ---------- listarLicensasSanitariasByEmpresa ----------

    @Test
    @DisplayName("Deve listar licenças por empresa")
    void deveListarLicensasPorEmpresa() {
        Empresa empresa = new Empresa();
        empresa.setLicensasSanitarias(List.of(new LicensaSanitaria()));

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaMapper.toResponse(any())).thenReturn(mock(LicensaSanitariaResponseDto.class));

        List<LicensaSanitariaResponseDto> result = licensaSanitariaService.listarLicensasSanitariasByEmpresa(1L);

        assertThat(result).hasSize(1);
    }

    // ---------- saveLicensaSanitariaByEmpresa ----------

    @Test
    @DisplayName("Deve salvar licença sanitária para empresa")
    void deveSalvarLicensaPorEmpresa() {
        Empresa empresa = new Empresa();
        LicensaSanitaria licensa = new LicensaSanitaria();
        LicensaSanitariaCadastroDto dto = mock(LicensaSanitariaCadastroDto.class);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaMapper.toEntity(dto)).thenReturn(licensa);
        when(licensaSanitariaRepository.save(licensa)).thenReturn(licensa);
        when(licensaSanitariaMapper.toResponse(licensa)).thenReturn(mock(LicensaSanitariaResponseDto.class));

        LicensaSanitariaResponseDto result = licensaSanitariaService.saveLicensaSanitariaByEmpresa(1L, dto);

        assertThat(result).isNotNull();
        assertThat(licensa.getEmpresa()).isEqualTo(empresa);
    }

    // ---------- emitirAlvara ----------

    @Test
    @DisplayName("Deve emitir alvará com sucesso quando empresa ativa e sem licença existente")
    void deveEmitirAlvaraComSucesso() {
        Empresa empresa = criarEmpresaAtiva();
        String numControle = "2026-001";

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaRepository.existsByEmpresaIdAndStatus(1L, StatusLicensa.ATIVA)).thenReturn(false);
        when(licensaSanitariaRepository.findByNumControle(numControle)).thenReturn(Optional.empty());
        when(licensaSanitariaRepository.save(any(LicensaSanitaria.class))).thenAnswer(inv -> inv.getArgument(0));
        when(empresaMapper.toResponse(empresa)).thenReturn(mock(EmpresaResponseDto.class));
        when(licensaSanitariaMapper.toResponse(any())).thenReturn(mock(LicensaSanitariaResponseDto.class));
        when(geradorPdfService.gerarLicensaSanitariaPdf(any(), any())).thenReturn(new byte[]{1, 2, 3});

        byte[] result = licensaSanitariaService.emitirAlvara(1L, numControle);

        assertThat(result).isNotEmpty();
        verify(licensaSanitariaRepository).save(any(LicensaSanitaria.class));
        verify(geradorPdfService).gerarLicensaSanitariaPdf(any(), any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando empresa não encontrada")
    void deveLancarExcecaoEmpresaNaoEncontrada() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> licensaSanitariaService.emitirAlvara(999L, "123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Empresa não encontrada");
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando empresa está inativa")
    void deveLancarExcecaoEmpresaInativa() {
        Empresa empresa = criarEmpresaAtiva();
        empresa.setAtivo(false);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        assertThatThrownBy(() -> licensaSanitariaService.emitirAlvara(1L, "123"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inativa");
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando empresa já possui licença ativa")
    void deveLancarExcecaoLicencaJaAtiva() {
        Empresa empresa = criarEmpresaAtiva();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaRepository.existsByEmpresaIdAndStatus(1L, StatusLicensa.ATIVA)).thenReturn(true);

        assertThatThrownBy(() -> licensaSanitariaService.emitirAlvara(1L, "123"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("licença sanitária ativa");
    }

    @Test
    @DisplayName("Deve lançar DuplicateResourceException quando número de controle já existe")
    void deveLancarExcecaoNumControleDuplicado() {
        Empresa empresa = criarEmpresaAtiva();
        String numControle = "DUPLICADO-001";

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaRepository.existsByEmpresaIdAndStatus(1L, StatusLicensa.ATIVA)).thenReturn(false);
        when(licensaSanitariaRepository.findByNumControle(numControle)).thenReturn(Optional.of(new LicensaSanitaria()));

        assertThatThrownBy(() -> licensaSanitariaService.emitirAlvara(1L, numControle))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(numControle);
    }

    // ---------- reemitirLicensa ----------

    @Test
    @DisplayName("Deve reemitir licença com sucesso")
    void deveReemitirLicensaComSucesso() {
        Empresa empresa = criarEmpresaAtiva();
        LicensaSanitaria licensaAntiga = new LicensaSanitaria();
        licensaAntiga.setId(10L);
        licensaAntiga.setStatus(StatusLicensa.ATIVA);

        String novoNumControle = "2026-002";
        String motivo = "Correção CNPJ";

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaRepository.findFirstByEmpresaIdAndStatus(1L, StatusLicensa.ATIVA)).thenReturn(Optional.of(licensaAntiga));
        when(licensaSanitariaRepository.findByNumControle(novoNumControle)).thenReturn(Optional.empty());
        when(licensaSanitariaRepository.save(any(LicensaSanitaria.class))).thenAnswer(inv -> {
            LicensaSanitaria salva = inv.getArgument(0);
            if(salva.getId() == null) salva.setId(20L); // Simulando ID da nova
            return salva;
        });
        when(geradorPdfService.gerarLicensaSanitariaPdf(any(), any())).thenReturn(new byte[]{1, 2, 3});

        byte[] result = licensaSanitariaService.reemitirLicensa(1L, novoNumControle, motivo);

        assertThat(result).isNotEmpty();
        assertThat(licensaAntiga.getStatus()).isEqualTo(StatusLicensa.CANCELADA);
        assertThat(licensaAntiga.getMotivoCancelamento()).isEqualTo(motivo);
        assertThat(licensaAntiga.getLicensaSubstitutaId()).isEqualTo(20L);
        // Save chamado 2 vezes: uma pra nova, uma pra antiga
        verify(licensaSanitariaRepository, times(2)).save(any(LicensaSanitaria.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao reemitir sem licença ativa")
    void deveLancarExcecaoReemitirSemLicensaAtiva() {
        Empresa empresa = criarEmpresaAtiva();
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaRepository.findFirstByEmpresaIdAndStatus(1L, StatusLicensa.ATIVA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> licensaSanitariaService.reemitirLicensa(1L, "NOVO", "MOTIVO"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nenhuma licença ativa encontrada");
    }

    // ---------- cancelarLicensa ----------

    @Test
    @DisplayName("Deve cancelar licença ativa com sucesso")
    void deveCancelarLicensaComSucesso() {
        LicensaSanitaria licensa = new LicensaSanitaria();
        licensa.setId(10L);
        licensa.setStatus(StatusLicensa.ATIVA);
        String motivo = "Cancelada pelo usuario";

        when(licensaSanitariaRepository.findById(10L)).thenReturn(Optional.of(licensa));
        when(licensaSanitariaRepository.save(licensa)).thenReturn(licensa);
        when(licensaSanitariaMapper.toResponse(licensa)).thenReturn(mock(LicensaSanitariaResponseDto.class));

        LicensaSanitariaResponseDto result = licensaSanitariaService.cancelarLicensa(10L, motivo);

        assertThat(result).isNotNull();
        assertThat(licensa.getStatus()).isEqualTo(StatusLicensa.CANCELADA);
        assertThat(licensa.getMotivoCancelamento()).isEqualTo(motivo);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cancelar licença que não está ativa")
    void deveLancarExcecaoAoCancelarLicensaJaCancelada() {
        LicensaSanitaria licensa = new LicensaSanitaria();
        licensa.setId(10L);
        licensa.setStatus(StatusLicensa.CANCELADA);

        when(licensaSanitariaRepository.findById(10L)).thenReturn(Optional.of(licensa));

        assertThatThrownBy(() -> licensaSanitariaService.cancelarLicensa(10L, "Motivo"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Apenas licenças ativas podem ser canceladas");
    }


    // ---------- imprimirLicensa ----------

    @Test
    @DisplayName("Deve reimprimir licença existente com sucesso")
    void deveImprimirLicensaExistente() {
        Empresa empresa = criarEmpresaAtiva();
        LicensaSanitaria licensa = new LicensaSanitaria();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaRepository.findFirstByEmpresaIdAndStatus(1L, StatusLicensa.ATIVA)).thenReturn(Optional.of(licensa));
        when(empresaMapper.toResponse(empresa)).thenReturn(mock(EmpresaResponseDto.class));
        when(licensaSanitariaMapper.toResponse(licensa)).thenReturn(mock(LicensaSanitariaResponseDto.class));
        when(geradorPdfService.gerarLicensaSanitariaPdf(any(), any())).thenReturn(new byte[]{1, 2, 3});

        byte[] result = licensaSanitariaService.imprimirLicensa(1L);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Deve lançar exceção ao imprimir licença para empresa sem licença ativa")
    void deveLancarExcecaoSemLicensaAtivaImprmir() {
        Empresa empresa = criarEmpresaAtiva();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaRepository.findFirstByEmpresaIdAndStatus(1L, StatusLicensa.ATIVA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> licensaSanitariaService.imprimirLicensa(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("licença ativa");
    }

    // ---------- helpers ----------

    private Empresa criarEmpresaAtiva() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setRazaoSocial("Empresa Teste");
        empresa.setAtivo(true);
        return empresa;
    }
}
