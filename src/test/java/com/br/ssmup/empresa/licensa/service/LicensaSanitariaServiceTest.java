package com.br.ssmup.empresa.licensa.service;

import com.br.ssmup.empresa.cnae.entity.Cnae;
import com.br.ssmup.empresa.cnae.enums.RiscoSanitario;
import com.br.ssmup.core.exception.BusinessRuleException;
import com.br.ssmup.core.exception.HighRiskInspectionException;
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

    @Test
    @DisplayName("Deve listar licenças sanitárias")
    void deveListarLicensas() {
        when(licensaSanitariaRepository.findAll()).thenReturn(List.of(new LicensaSanitaria()));
        when(licensaSanitariaMapper.toResponse(any())).thenReturn(mock(LicensaSanitariaResponseDto.class));

        List<LicensaSanitariaResponseDto> result = licensaSanitariaService.buscarLicensasSanitaria();

        assertThat(result).hasSize(1);
    }

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

//    @Test
//    @DisplayName("Deve emitir alvará para empresa de baixo risco")
//    void deveEmitirAlvara() {
//        Empresa empresa = new Empresa();
//        empresa.setId(1L);
//        Cnae cnae = new Cnae();
//        cnae.setRisco(RiscoSanitario.RISCO_I_BAIXO);
//        empresa.setCnaePrincipal(cnae);
//
//        LicensaSanitaria licensa = new LicensaSanitaria();
//        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
//        when(licensaSanitariaRepository.findFirstByEmpresaIdAndStatusTrue(1L)).thenReturn(Optional.of(licensa));
//        when(empresaMapper.toResponse(empresa)).thenReturn(mock(EmpresaResponseDto.class));
//        when(licensaSanitariaMapper.toResponse(licensa)).thenReturn(mock(LicensaSanitariaResponseDto.class));
//        when(geradorPdfService.gerarLicensaSanitariaPdf(any(), any())).thenReturn(new byte[]{1, 2, 3});
//
//        byte[] result = licensaSanitariaService.emitirAlvara(1L);
//
//        assertThat(result).isNotEmpty();
//    }

//    @Test
//    @DisplayName("Deve lançar exceção para empresa sem CNAE")
//    void deveLancarExcecaoSemCnae() {
//        Empresa empresa = new Empresa();
//        empresa.setCnaePrincipal(null);
//
//        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
//
//        assertThatThrownBy(() -> licensaSanitariaService.emitirAlvara(1L))
//                .isInstanceOf(BusinessRuleException.class);
//    }

//    @Test
//    @DisplayName("Deve lançar exceção para alto risco sem inspeção")
//    void deveLancarExcecaoAltoRiscoSemInspecao() {
//        Empresa empresa = new Empresa();
//        Cnae cnae = new Cnae();
//        cnae.setRisco(RiscoSanitario.RISCO_III_ALTO);
//        empresa.setCnaePrincipal(cnae);
//        empresa.setInspecao(false);
//
//        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
//
//        assertThatThrownBy(() -> licensaSanitariaService.emitirAlvara(1L))
//                .isInstanceOf(HighRiskInspectionException.class);
//    }
}
