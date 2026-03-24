package com.br.ssmup.empresa.endereco.service;

import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.cadastro.repository.EmpresaRepository;
import com.br.ssmup.empresa.endereco.dto.EnderecoAtualizarDto;
import com.br.ssmup.empresa.endereco.dto.EnderecoResponseDto;
import com.br.ssmup.empresa.endereco.entity.Endereco;
import com.br.ssmup.empresa.endereco.mapper.EnderecoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock private EmpresaRepository empresaRepository;
    @Mock private EnderecoMapper enderecoMapper;

    @InjectMocks
    private EnderecoService enderecoService;

    @Test
    @DisplayName("Deve atualizar endereço usando mapper")
    void deveAtualizarEndereco() {
        Empresa empresa = new Empresa();
        Endereco endereco = new Endereco();
        empresa.setEndereco(endereco);
        EnderecoAtualizarDto dto = mock(EnderecoAtualizarDto.class);
        EnderecoResponseDto responseDto = mock(EnderecoResponseDto.class);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(enderecoMapper.toResponse(endereco)).thenReturn(responseDto);

        EnderecoResponseDto result = enderecoService.atualizarEndereco(1L, dto);

        assertThat(result).isNotNull();
        verify(enderecoMapper).updateFromDto(dto, endereco);
        verify(empresaRepository).save(empresa);
    }

    @Test
    @DisplayName("Deve lançar exceção para empresa inexistente")
    void deveLancarExcecaoEmpresaNaoEncontrada() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enderecoService.atualizarEndereco(999L, mock(EnderecoAtualizarDto.class)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
