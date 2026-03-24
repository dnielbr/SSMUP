package com.br.ssmup.auth.usuario.dto;

import com.br.ssmup.auth.usuario.enums.Role;

import java.io.Serializable;

public record UsuarioResponseDto(
        Long id,
        String nome,
        String email,
        String cargo,
        String matricula,
        Role role,
        boolean ativo,
        boolean emailVerificado
) implements Serializable {
}
