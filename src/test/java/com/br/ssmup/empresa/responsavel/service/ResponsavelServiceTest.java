package com.br.ssmup.empresa.responsavel.service;

import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.cadastro.mapper.EmpresaMapper;
import com.br.ssmup.empresa.cadastro.repository.EmpresaRepository;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelAtualizarDto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelCadastroDto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelResponseDto;
import com.br.ssmup.empresa.responsavel.entity.Responsavel;
import com.br.ssmup.empresa.responsavel.mapper.ResponsavelMapper;
import com.br.ssmup.empresa.responsavel.repository.ResponsavelRepository;
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
class ResponsavelServiceTest {

    @Mock private ResponsavelRepository responsavelRepository;
    @Mock private ResponsavelMapper responsavelMapper;
    @Mock private EmpresaMapper empresaMapper;
    @Mock private EmpresaRepository empresaRepository;

    @InjectMocks
    private ResponsavelService responsavelService;

    @Test
    @DisplayName("Deve listar responsáveis")
    void deveListarResponsaveis() {
        when(responsavelRepository.findAll()).thenReturn(List.of(new Responsavel()));
        when(responsavelMapper.toResponse(any())).thenReturn(mock(ResponsavelResponseDto.class));

        List<ResponsavelResponseDto> result = responsavelService.listarResponsaveis();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve salvar responsável")
    void deveSalvarResponsavel() {
        ResponsavelCadastroDto dto = mock(ResponsavelCadastroDto.class);
        Responsavel responsavel = new Responsavel();
        ResponsavelResponseDto responseDto = mock(ResponsavelResponseDto.class);

        when(responsavelMapper.toEntity(dto)).thenReturn(responsavel);
        when(responsavelMapper.toResponse(responsavel)).thenReturn(responseDto);

        ResponsavelResponseDto result = responsavelService.salvar(dto);

        assertThat(result).isNotNull();
        verify(responsavelRepository).save(responsavel);
    }

    @Test
    @DisplayName("Deve buscar responsável por ID")
    void deveBuscarById() {
        Responsavel resp = new Responsavel();
        when(responsavelRepository.findById(1L)).thenReturn(Optional.of(resp));
        when(responsavelMapper.toResponse(resp)).thenReturn(mock(ResponsavelResponseDto.class));

        ResponsavelResponseDto result = responsavelService.buscarResponsavelById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar responsável inexistente")
    void deveLancarExcecaoResponsavelNaoEncontrado() {
        when(responsavelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> responsavelService.buscarResponsavelById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve atualizar responsável usando mapper")
    void deveAtualizarResponsavel() {
        Empresa empresa = new Empresa();
        Responsavel resp = new Responsavel();
        empresa.setResponsavel(resp);
        ResponsavelAtualizarDto dto = mock(ResponsavelAtualizarDto.class);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(responsavelMapper.toResponse(resp)).thenReturn(mock(ResponsavelResponseDto.class));

        responsavelService.atualizarResponsavel(1L, dto);

        verify(responsavelMapper).updateFromDto(dto, resp);
        verify(empresaRepository).save(empresa);
    }
}
