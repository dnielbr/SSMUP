package com.br.ssmup.auth.service;

import com.br.ssmup.auth.component.GoogleTokenVerifier;
import com.br.ssmup.core.exception.BusinessRuleException;
import com.br.ssmup.core.exception.UnauthorizedException;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.auth.usuario.enums.Role;
import com.br.ssmup.auth.usuario.repository.UsuarioRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TokenService tokenService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private GoogleTokenVerifier googleTokenVerifier;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve realizar login Google com sucesso")
    void deveRealizarLoginGoogleComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");
        usuario.setNome("Test User");
        usuario.setRole(Role.ADMIN);
        usuario.setAtivo(true);
        usuario.setEmailVerificado(true);

        var refreshToken = new com.br.ssmup.auth.entity.RefreshToken();
        refreshToken.setToken("refresh-token-123");

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("user@test.com");

        when(googleTokenVerifier.verify("google-token-123")).thenReturn(payload);
        when(usuarioRepository.findByEmailAndAtivoAndEmailVerificado("user@test.com", true, true))
                .thenReturn(Optional.of(usuario));
        when(tokenService.gerarToken(usuario)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(usuario)).thenReturn(refreshToken);
        when(tokenService.getExpiration()).thenReturn(3600L);

        com.br.ssmup.auth.dto.AuthResponse result = authService.loginGoogle("google-token-123");

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-123");
        verify(googleTokenVerifier).verify("google-token-123");
        verify(refreshTokenService).createRefreshToken(usuario);
    }

    @Test
    @DisplayName("Deve atualizar token via refresh token")
    void deveAtualizarTokenViaRefreshToken() {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@test.com");

        var oldRefreshToken = new com.br.ssmup.auth.entity.RefreshToken();
        oldRefreshToken.setUsuario(usuario);
        oldRefreshToken.setToken("old-refresh-token");

        var newRefreshToken = new com.br.ssmup.auth.entity.RefreshToken();
        newRefreshToken.setToken("new-refresh-token");

        when(refreshTokenService.findByToken("old-refresh-token")).thenReturn(oldRefreshToken);
        when(refreshTokenService.createRefreshToken(usuario)).thenReturn(newRefreshToken);
        when(tokenService.gerarToken(usuario)).thenReturn("new-jwt-token");
        when(tokenService.getExpiration()).thenReturn(3600L);

        com.br.ssmup.auth.dto.AuthResponse result = authService.refreshToken("old-refresh-token");

        assertThat(result.accessToken()).isEqualTo("new-jwt-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenService).verifyExpiration(oldRefreshToken);
    }

    @Test
    @DisplayName("Deve realizar logout")
    void deveRealizarLogout() {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@test.com");

        authService.logout(usuario);

        verify(refreshTokenService).revokeByUser(usuario);
    }

    @Test
    @DisplayName("Deve lançar exceção para usuário não encontrado no Google login")
    void deveLancarExcecaoUsuarioNaoEncontrado() {
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("unknown@test.com");

        when(googleTokenVerifier.verify("google-token-123")).thenReturn(payload);
        when(usuarioRepository.findByEmailAndAtivoAndEmailVerificado("unknown@test.com", true, true))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loginGoogle("google-token-123"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve ativar conta com token válido")
    void deveAtivarContaComTokenValido() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setTokenAtivacao("valid-token");
        usuario.setDataExpiracaoToken(LocalDateTime.now().plusHours(1));

        when(usuarioRepository.findByTokenAtivacao("valid-token")).thenReturn(Optional.of(usuario));

        authService.ativarConta("valid-token");

        assertThat(usuario.isEmailVerificado()).isTrue();
        assertThat(usuario.getTokenAtivacao()).isNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar exceção para token de ativação inválido")
    void deveLancarExcecaoTokenInvalido() {
        when(usuarioRepository.findByTokenAtivacao("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.ativarConta("invalid-token"))
                .isInstanceOf(BusinessRuleException.class);
    }
}
