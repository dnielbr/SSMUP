package com.br.ssmup.empresa.inspecao.service;

import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.cadastro.repository.EmpresaRepository;
import com.br.ssmup.empresa.inspecao.dto.InspecaoRelatorioRequestDto;
import com.br.ssmup.empresa.inspecao.dto.InspecaoRelatorioResponseDto;
import com.br.ssmup.empresa.inspecao.dto.InspecaoRelatorioUpdateDto;
import com.br.ssmup.empresa.inspecao.entity.InspecaoRelatorio;
import com.br.ssmup.empresa.inspecao.enums.StatusInspecao;
import com.br.ssmup.empresa.inspecao.mapper.InspecaoRelatorioMapper;
import com.br.ssmup.empresa.inspecao.repository.InspecaoRelatorioRepository;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.auth.usuario.repository.UsuarioRepository;
import com.br.ssmup.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspecaoRelatorioServiceTest {

    @Mock private InspecaoRelatorioRepository inspecaoRelatorioRepository;
    @Mock private InspecaoRelatorioMapper inspecaoRelatorioMapper;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private InspecaoRelatorioService inspecaoRelatorioService;

    @Test
    @DisplayName("Deve listar todos os relatórios de inspeção")
    void deveListarRelatorios() {
        when(inspecaoRelatorioRepository.findAll()).thenReturn(List.of(new InspecaoRelatorio()));
        when(inspecaoRelatorioMapper.toDto(any())).thenReturn(mock(InspecaoRelatorioResponseDto.class));

        List<InspecaoRelatorioResponseDto> result = inspecaoRelatorioService.listarInspecaoRelatorio();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve listar relatórios por ID da empresa")
    void deveListarPorEmpresa() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(new Empresa()));
        when(inspecaoRelatorioRepository.findAllByEmpresaId(1L)).thenReturn(List.of(new InspecaoRelatorio()));
        when(inspecaoRelatorioMapper.toDto(any())).thenReturn(mock(InspecaoRelatorioResponseDto.class));

        List<InspecaoRelatorioResponseDto> result = inspecaoRelatorioService.listarInspecaoRelatorioByEmpresaId(1L);

        assertThat(result).hasSize(1);
    }

//    @Test
//    @DisplayName("Deve salvar relatório de inspeção e atualizar status da empresa se aprovada")
//    void deveSalvarInspecaoEAprovarEmpresa() {
//        InspecaoRelatorioRequestDto dto = mock(InspecaoRelatorioRequestDto.class);
//        Empresa empresa = new Empresa();
//        empresa.setInspecao(false);
//
//        when(dto.empresaId()).thenReturn(1L);
//        when(dto.statusInspecao()).thenReturn(StatusInspecao.APROVADA);
//        when(dto.usuariosId()).thenReturn(List.of(1L));
//        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
//        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);
//        when(usuarioRepository.findAllById(anyList())).thenReturn(List.of(new Usuario()));
//        when(inspecaoRelatorioRepository.save(any(InspecaoRelatorio.class))).thenReturn(new InspecaoRelatorio());
//        when(inspecaoRelatorioMapper.toDto(any())).thenReturn(mock(InspecaoRelatorioResponseDto.class));
//
//        inspecaoRelatorioService.salvarInspecaoRelatorio(dto);
//
//        assertThat(empresa.isInspecao()).isTrue();
//        verify(empresaRepository).save(empresa);
//        verify(inspecaoRelatorioRepository).save(any(InspecaoRelatorio.class));
//    }

//    @Test
//    @DisplayName("Deve lançar exceção ao salvar relatório sem usuários")
//    void deveLancarExcecaoSemUsuarios() {
//        InspecaoRelatorioRequestDto dto = mock(InspecaoRelatorioRequestDto.class);
//        when(dto.empresaId()).thenReturn(1L);
//        when(dto.statusInspecao()).thenReturn(StatusInspecao.APROVADA);
//        when(dto.usuariosId()).thenReturn(Collections.emptyList());
//        when(empresaRepository.findById(1L)).thenReturn(Optional.of(new Empresa()));
//        when(usuarioRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
//
//        assertThatThrownBy(() -> inspecaoRelatorioService.salvarInspecaoRelatorio(dto))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Pelo menos um id de usuario deve ser fornecido");
//    }

    @Test
    @DisplayName("Deve atualizar relatório de inspeção usando mapper")
    void deveAtualizarRelatorio() {
        InspecaoRelatorio relatorio = new InspecaoRelatorio();
        InspecaoRelatorioUpdateDto dto = mock(InspecaoRelatorioUpdateDto.class);

        when(inspecaoRelatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));
        when(inspecaoRelatorioRepository.save(relatorio)).thenReturn(relatorio);
        when(inspecaoRelatorioMapper.toDto(relatorio)).thenReturn(mock(InspecaoRelatorioResponseDto.class));

        inspecaoRelatorioService.atualizarInspecaoRelatorio(1L, dto);

        verify(inspecaoRelatorioMapper).updateFromDto(dto, relatorio);
        verify(inspecaoRelatorioRepository).save(relatorio);
    }
}
