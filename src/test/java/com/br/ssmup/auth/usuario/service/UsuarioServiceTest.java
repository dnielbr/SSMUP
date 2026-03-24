package com.br.ssmup.auth.usuario.service;

import com.br.ssmup.core.exception.BusinessRuleException;
import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.auth.usuario.dto.UsuarioAtualizarDto;
import com.br.ssmup.auth.usuario.dto.UsuarioCadastroDto;
import com.br.ssmup.auth.usuario.dto.UsuarioResponseDto;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.auth.usuario.mapper.UsuarioMapper;
import com.br.ssmup.auth.usuario.repository.UsuarioRepository;
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
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private EmailService emailService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(new Usuario()));
        when(usuarioMapper.toResponse(any())).thenReturn(mock(UsuarioResponseDto.class));

        List<UsuarioResponseDto> result = usuarioService.listarUsuarios();

        assertThat(result).hasSize(1);
        verify(usuarioRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar usuários por filtro ativo")
    void deveListarUsuariosPorFiltro() {
        when(usuarioRepository.findByAtivo(true)).thenReturn(List.of(new Usuario()));
        when(usuarioMapper.toResponse(any())).thenReturn(mock(UsuarioResponseDto.class));

        List<UsuarioResponseDto> result = usuarioService.listarUsuariosByFilter(true);

        assertThat(result).hasSize(1);
        verify(usuarioRepository).findByAtivo(true);
    }

    @Test
    @DisplayName("Deve salvar usuário e enviar e-mail")
    void deveSalvarUsuarioEEnviarEmail() {
        UsuarioCadastroDto dto = mock(UsuarioCadastroDto.class);
        Usuario usuario = new Usuario();
        usuario.setEmail("test@test.com");
        usuario.setNome("Test");

        when(usuarioMapper.toEntity(dto)).thenReturn(usuario);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(mock(UsuarioResponseDto.class));

        usuarioService.salvarUsuario(dto);

        assertThat(usuario.getTokenAtivacao()).isNotNull();
        assertThat(usuario.getDataExpiracaoToken()).isNotNull();
        verify(usuarioRepository).save(usuario);
        verify(emailService).enviarEmailAtivacao(eq("test@test.com"), eq("Test"), anyString());
    }

    @Test
    @DisplayName("Deve reenviar e-mail de ativação")
    void deveReenviarEmailAtivacao() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setAtivo(false);
        usuario.setEmail("test@test.com");
        usuario.setNome("Test");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.reenviarEmailAtivacao(1L);

        assertThat(usuario.getTokenAtivacao()).isNotNull();
        verify(usuarioRepository).save(usuario);
        verify(emailService).enviarEmailAtivacao(eq("test@test.com"), eq("Test"), anyString());
    }

    @Test
    @DisplayName("Não deve reenviar e-mail se usuário já estiver ativo")
    void naoDeveReenviarEmailSeUsuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.reenviarEmailAtivacao(1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Deve inativar usuário")
    void deveInativarUsuario() {
        Usuario usuario = new Usuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.inativarUsuarioById(1L);

        assertThat(usuario.isAtivo()).isFalse();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve atualizar usuário usando mapper")
    void deveAtualizarUsuario() {
        Usuario usuario = new Usuario();
        UsuarioAtualizarDto dto = mock(UsuarioAtualizarDto.class);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(mock(UsuarioResponseDto.class));

        usuarioService.atualizarUsuario(1L, dto);

        verify(usuarioMapper).updateFromDto(dto, usuario);
        verify(usuarioRepository).save(usuario);
    }
}
