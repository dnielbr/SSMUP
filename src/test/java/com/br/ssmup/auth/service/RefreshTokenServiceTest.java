package com.br.ssmup.auth.service;

import com.br.ssmup.auth.entity.RefreshToken;
import com.br.ssmup.auth.repository.RefreshTokenRepository;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.core.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", 604800000L);
    }

    @Test
    @DisplayName("Deve criar um refresh token com sucesso")
    void deveCriarRefreshTokenComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(usuario);

        assertThat(result).isNotNull();
        assertThat(result.getUsuario()).isEqualTo(usuario);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiryDate()).isAfter(Instant.now());
        verify(refreshTokenRepository).deleteByUsuario(usuario);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve validar expiração do token - Token Válido")
    void deveValidarExpiracaoTokenValido() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().plusSeconds(100));

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("Deve lançar exceção para token expirado")
    void deveLancarExcecaoTokenExpirado() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(100));

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                .isInstanceOf(UnauthorizedException.class);
        
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    @DisplayName("Deve encontrar token via string")
    void deveEncontrarTokenViaString() {
        RefreshToken token = new RefreshToken();
        token.setToken("some-uuid");

        when(refreshTokenRepository.findByToken("some-uuid")).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.findByToken("some-uuid");

        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("Deve lançar exceção se token não for encontrado")
    void deveLancarExcecaoTokenNaoEncontrado() {
        when(refreshTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.findByToken("invalid"))
                .isInstanceOf(UnauthorizedException.class);
    }
}
