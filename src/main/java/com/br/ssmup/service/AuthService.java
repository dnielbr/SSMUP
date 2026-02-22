package com.br.ssmup.service;

import com.br.ssmup.components.GoogleTokenVerifier;
import com.br.ssmup.entities.Usuario;
import com.br.ssmup.exceptions.UnauthorizedException;
import com.br.ssmup.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthService(UsuarioRepository usuarioRepository, TokenService tokenService, GoogleTokenVerifier googleTokenVerifier) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    public String loginGoogle(String googleToken) {
        log.info("Iniciando login com google");
        var payload = googleTokenVerifier.verify(googleToken);

        String email = payload.getEmail();

        Usuario usuario = usuarioRepository.findByEmailAndAtivo(email, true)
                .orElseThrow( () -> {
                    log.error("Falha ao validar usuario com email {}", email);
                    return new UnauthorizedException("Usuário não autorizado");
                });

        log.info("Login sucesso com email: {}, gerando token jwt!", email);
        return tokenService.gerarToken(usuario);

    }
}
