package com.br.ssmup.auth.usuario.controller;

import com.br.ssmup.auth.usuario.dto.UsuarioAtualizarDto;
import com.br.ssmup.auth.usuario.dto.UsuarioCadastroDto;
import com.br.ssmup.auth.usuario.dto.UsuarioResponseDto;
import com.br.ssmup.auth.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/api/usuarios")
public class UsuarioController {

    private UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDto>> getAllUsuarios(){
        return ResponseEntity.ok().body(usuarioService.listarUsuarios());
    }

    @GetMapping("filter")
    public ResponseEntity<List<UsuarioResponseDto>> getallUsuariosByFilter(
        @RequestParam(required = false) Boolean ativo
    ){
        return ResponseEntity.ok().body(usuarioService.listarUsuariosByFilter(ativo));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDto> saveUsuario(@RequestBody @Valid UsuarioCadastroDto payload){
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.salvarUsuario(payload));
    }

    @DeleteMapping("{id}/inativar")
    public ResponseEntity<Void> inativarUsuario(@PathVariable Long id){
        usuarioService.inativarUsuarioById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("{id}/ativar")
    public ResponseEntity<Void> ativarUsuario(@PathVariable Long id){
        usuarioService.ativarUsuarioById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("{id}")
    public ResponseEntity<UsuarioResponseDto> putUsuario(@PathVariable Long id, @RequestBody @Valid UsuarioAtualizarDto payload){
        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, payload));
    }

    @PostMapping("{id}/reenviar-ativacao")
    @Operation(summary = "Reenviar e-mail de ativação", description = "Gera novo token e reenvia o e-mail de ativação para o usuário.")
    public ResponseEntity<Void> reenviarEmailAtivacao(@PathVariable Long id) {
        usuarioService.reenviarEmailAtivacao(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
