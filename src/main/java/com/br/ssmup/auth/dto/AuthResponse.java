package com.br.ssmup.auth.dto;

public record AuthResponse(String token, String refreshToken, String type, long expiresIn) {
}
