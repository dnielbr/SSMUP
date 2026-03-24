package com.br.ssmup.auth.usuario.dto;

import com.br.ssmup.auth.usuario.enums.Role;

public record UsuarioAtualizarDto(
        String nome,
        String email,
        String cargo,
        String matricula,
        Role role
) {
}
