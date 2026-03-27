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
        when(licensaSanitariaRepository.existsByEmpresaIdAndStatusTrue(1L)).thenReturn(false);
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
        when(licensaSanitariaRepository.existsByEmpresaIdAndStatusTrue(1L)).thenReturn(true);

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
        when(licensaSanitariaRepository.existsByEmpresaIdAndStatusTrue(1L)).thenReturn(false);
        when(licensaSanitariaRepository.findByNumControle(numControle)).thenReturn(Optional.of(new LicensaSanitaria()));

        assertThatThrownBy(() -> licensaSanitariaService.emitirAlvara(1L, numControle))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(numControle);
    }

    // ---------- imprimirLicensa ----------

    @Test
    @DisplayName("Deve reimprimir licença existente com sucesso")
    void deveImprimirLicensaExistente() {
        Empresa empresa = criarEmpresaAtiva();
        LicensaSanitaria licensa = new LicensaSanitaria();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaRepository.findFirstByEmpresaIdAndStatusTrue(1L)).thenReturn(Optional.of(licensa));
        when(empresaMapper.toResponse(empresa)).thenReturn(mock(EmpresaResponseDto.class));
        when(licensaSanitariaMapper.toResponse(licensa)).thenReturn(mock(LicensaSanitariaResponseDto.class));
        when(geradorPdfService.gerarLicensaSanitariaPdf(any(), any())).thenReturn(new byte[]{1, 2, 3});

        byte[] result = licensaSanitariaService.imprimirLicensa(1L);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Deve lançar exceção ao imprimir licença para empresa sem licença ativa")
    void deveLancarExcecaoSemLicensaAtiva() {
        Empresa empresa = criarEmpresaAtiva();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(licensaSanitariaRepository.findFirstByEmpresaIdAndStatusTrue(1L)).thenReturn(Optional.empty());

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
