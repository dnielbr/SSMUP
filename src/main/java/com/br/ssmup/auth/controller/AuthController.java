package com.br.ssmup.auth.controller;

import com.br.ssmup.auth.dto.AuthResponse;
import com.br.ssmup.auth.dto.GoogleLoginRequest;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.auth.usuario.repository.UsuarioRepository;
import com.br.ssmup.auth.service.AuthService;
import com.br.ssmup.auth.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import com.br.ssmup.auth.dto.TokenRefreshRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/api/auth")
@Tag(name = "Autenticação", description = "Gerenciamento de Login e Tokens (OAuth2/JWT)")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthService authService, TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/google")
    @Operation(summary = "Login com Google", description = "Autentica o usuário validando o token do Google e retorna um JWT da aplicação.")
    public ResponseEntity<AuthResponse> loginGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginGoogle(request.token());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Gera um novo access token e um novo refresh token a partir de um refresh token válido.")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        AuthResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalida o refresh token do usuário.")
    public ResponseEntity<Void> logout(java.security.Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new com.br.ssmup.core.exception.UnauthorizedException("Usuário não encontrado"));
        authService.logout(usuario);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ativar-conta")
    @Operation(summary = "Ativar conta", description = "Ativa a conta do usuario usando o token enviado por email.")
    public ResponseEntity<Map<String, String>> ativarConta(@RequestParam String token) {
        authService.ativarConta(token);
        return ResponseEntity.ok(Map.of("message", "Conta ativado com sucesso"));
    }
}
