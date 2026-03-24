package com.br.ssmup.auth.usuario.service;

import com.br.ssmup.auth.usuario.dto.UsuarioAtualizarDto;
import com.br.ssmup.auth.usuario.dto.UsuarioCadastroDto;
import com.br.ssmup.auth.usuario.dto.UsuarioResponseDto;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.auth.usuario.enums.Role;
import com.br.ssmup.core.exception.BusinessRuleException;
import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.auth.usuario.mapper.UsuarioMapper;
import com.br.ssmup.auth.usuario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final EmailService emailService;

    public List<UsuarioResponseDto> listarUsuarios(){
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    public List<UsuarioResponseDto> listarUsuariosByFilter(Boolean ativo){
        if(ativo == null){
            return usuarioRepository.findAll().stream()
                    .map(usuarioMapper::toResponse)
                    .toList();
        }
        return usuarioRepository.findByAtivo(ativo).stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    @Transactional
    public UsuarioResponseDto salvarUsuario(UsuarioCadastroDto dto){
        Usuario usuario = usuarioMapper.toEntity(dto);

        String token = UUID.randomUUID().toString();
        usuario.setTokenAtivacao(token);
        usuario.setDataExpiracaoToken(LocalDateTime.now().plusHours(24));

        Usuario salvo = usuarioRepository.save(usuario);

        emailService.enviarEmailAtivacao(salvo.getEmail(), salvo.getNome(), token);

        return usuarioMapper.toResponse(salvo);
    }

    @Transactional
    public void reenviarEmailAtivacao(Long id){
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario com id: " + id + " não encontrado"));

        if(usuario.isAtivo()){
            throw new BusinessRuleException("Usuario ja esta ativo. Nao é necessario reenviar o email de ativação");
        }

        String novoToken = UUID.randomUUID().toString();
        usuario.setTokenAtivacao(novoToken);
        usuario.setDataExpiracaoToken(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(usuario);

        emailService.enviarEmailAtivacao(usuario.getEmail(), usuario.getNome(), novoToken);
    }

    @Transactional
    public void inativarUsuarioById(Long id){
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario com id: " + id + " não econtrado"));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void ativarUsuarioById(Long id){
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario com id: " + id + " não econtrado"));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDto atualizarUsuario(Long id, UsuarioAtualizarDto dto){
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Usuario com id: " + id + "não encontrado"));
        usuarioMapper.updateFromDto(dto, usuario);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }
}
