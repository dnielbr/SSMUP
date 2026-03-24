package com.br.ssmup.auth.service;

import com.br.ssmup.auth.dto.AuthResponse;

import com.br.ssmup.auth.component.GoogleTokenVerifier;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.core.exception.BusinessRuleException;
import com.br.ssmup.core.exception.UnauthorizedException;
import com.br.ssmup.auth.usuario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UsuarioRepository usuarioRepository, TokenService tokenService, GoogleTokenVerifier googleTokenVerifier, RefreshTokenService refreshTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse loginGoogle(String googleToken) {
        log.info("Iniciando login com google");
        var payload = googleTokenVerifier.verify(googleToken);

        String email = payload.getEmail();

        Usuario usuario = usuarioRepository.findByEmailAndAtivoAndEmailVerificado(email, true, true)
                .orElseThrow( () -> {
                    log.error("Falha ao validar usuario com email {}", email);
                    return new UnauthorizedException("Usuário não autorizado");
                });

        log.info("Login sucesso com email: {}, gerando tokens JWT!", email);
        String accessToken = tokenService.gerarToken(usuario);
        var refreshToken = refreshTokenService.createRefreshToken(usuario);

        return new AuthResponse(accessToken, refreshToken.getToken(), "Bearer", tokenService.getExpiration());
    }

    @Transactional
    public AuthResponse refreshToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Refresh token ausente.");
        }
        log.info("Tentando atualizar token access via refresh token");
        var refreshToken = refreshTokenService.findByToken(token);
        refreshTokenService.verifyExpiration(refreshToken);

        Usuario usuario = refreshToken.getUsuario();
        
        // Deletar o token antigo (Rotação)
        refreshTokenService.delete(refreshToken);
        
        var newRefreshToken = refreshTokenService.createRefreshToken(usuario);
        String newAccessToken = tokenService.gerarToken(usuario);

        log.info("Token access atualizado com sucesso via refresh token para o usuario: {}", usuario.getEmail());
        return new AuthResponse(newAccessToken, newRefreshToken.getToken(), "Bearer", tokenService.getExpiration());
    }

    @Transactional
    public void logout(Usuario usuario) {
        log.info("Realizando logout para o usuario: {}", usuario.getEmail());
        refreshTokenService.revokeByUser(usuario);
    }

    @Transactional
    public void ativarConta(String token) {
        log.info("Tentado ativar conta com token: {}", token);

        Usuario usuario = usuarioRepository.findByTokenAtivacao(token).orElseThrow(() -> {
            log.error("token de ativação invalido: {}", token);
            return new BusinessRuleException("Token de ativação invalido");
        });

        if(usuario.getDataExpiracaoToken().isBefore(LocalDateTime.now())){
            log.error("Token de ativação expirado para o usuario: {}", usuario.getEmail());
            throw new BusinessRuleException("Token expirado para o usuario. Solicite um novo ao seu coodernador");
        }

        usuario.setTokenAtivacao(null);
        usuario.setDataExpiracaoToken(null);
        usuario.setAtivo(true);
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        log.info("Conta ativada com sucesso: {}", usuario.getEmail());
    }
}
