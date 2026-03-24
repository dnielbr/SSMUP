package com.br.ssmup.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record AuthResponse(String accessToken, @JsonIgnore String refreshToken, String type, long expiresIn) {
}
