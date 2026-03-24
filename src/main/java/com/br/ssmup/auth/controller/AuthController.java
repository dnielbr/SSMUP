package com.br.ssmup.auth.controller;

import com.br.ssmup.auth.dto.AuthResponse;
import com.br.ssmup.auth.dto.GoogleLoginRequest;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.auth.usuario.repository.UsuarioRepository;
import com.br.ssmup.auth.service.AuthService;
import com.br.ssmup.auth.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.br.ssmup.auth.dto.TokenRefreshRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/api/auth")
@Tag(name = "Autenticação", description = "Gerenciamento de Login e Tokens (OAuth2/JWT)")
public class AuthController {

    private final AuthService authService;

    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthService authService, UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/google")
    @Operation(summary = "Login com Google", description = "Autentica o usuário validando o token do Google e retorna um JWT da aplicação.")
    public ResponseEntity<AuthResponse> loginGoogle(@Valid @RequestBody GoogleLoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.loginGoogle(request.token());
        
        addRefreshTokenCookie(response, authResponse.refreshToken());
        
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Gera um novo access token e um novo refresh token a partir de um refresh token válido.")
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            throw new com.br.ssmup.core.exception.UnauthorizedException("Refresh token ausente.");
        }
        
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        
        addRefreshTokenCookie(response, authResponse.refreshToken());
        
        return ResponseEntity.ok(authResponse);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String token) {
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false) // MUDAR PARA TRUE EM PRODUÇÃO (HTTPS)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 dias
                .sameSite("Strict")
                .build();
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());
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
