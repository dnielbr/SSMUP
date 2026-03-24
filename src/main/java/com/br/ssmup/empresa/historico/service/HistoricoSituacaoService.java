package com.br.ssmup.empresa.historico.service;

import com.br.ssmup.core.exception.AuthenticationException;
import com.br.ssmup.core.exception.ResourceNotFoundException;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.historico.dto.HistoricoSituacaoResponseDto;
import com.br.ssmup.empresa.historico.entity.HistoricoSituacao;
import com.br.ssmup.empresa.historico.enums.TipoSituacao;
import com.br.ssmup.empresa.historico.mapper.HistoricoSituacaoMapper;
import com.br.ssmup.empresa.historico.repository.HistoricoSituacaoRepository;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.auth.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoSituacaoService {

    private final HistoricoSituacaoRepository historicoSituacaoRepository;
    private final HistoricoSituacaoMapper historicoSituacaoMapper;
    private final UsuarioRepository usuarioRepository;

    public void gravarHistoricoSituacao(String motivo, Empresa empresa, TipoSituacao tipoSituacao) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("Nenhum usuario autenticado encontrado");
        }

        Usuario usuario = usuarioRepository.findByEmail((String) authentication.getPrincipal())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario do token nao existe no banco"));

        HistoricoSituacao historicoSituacao = new HistoricoSituacao();
        historicoSituacao.setUsuario(usuario);
        historicoSituacao.setEmpresa(empresa);
        historicoSituacao.setMotivo(motivo);
        historicoSituacao.setTipoSituacao(tipoSituacao);
        historicoSituacaoRepository.save(historicoSituacao);
    }

    public List<HistoricoSituacaoResponseDto> listarHistoricoSituacao(Long empresaId) {
        return historicoSituacaoRepository.findByEmpresaIdOrderByDataDesc(empresaId).stream()
                .map(historicoSituacaoMapper::toDto)
                .toList();
    }
}
