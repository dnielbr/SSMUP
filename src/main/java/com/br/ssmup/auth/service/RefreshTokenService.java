package com.br.ssmup.auth.service;

import com.br.ssmup.auth.entity.RefreshToken;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.auth.repository.RefreshTokenRepository;
import com.br.ssmup.core.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(Usuario usuario) {
        // Exclui tokens anteriores para esse usuário (uma sessão por usuário, ou ajuste conforme necessário)
        // Para aplicações grandes, pode-se permitir múltiplos tokens (múltiplos dispositivos)
        // Aqui seguiremos a regra de um por usuário para simplicidade, mas com suporte a rotação.
        refreshTokenRepository.deleteByUsuario(usuario);

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token expirado. Por favor, faça login novamente.");
        }
        return token;
    }

    @Transactional
    public void revokeByUser(Usuario usuario) {
        refreshTokenRepository.deleteByUsuario(usuario);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh token não encontrado."));
    }
}
