package com.br.ssmup.empresa.historico.service;

import com.br.ssmup.core.exception.AuthenticationException;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.historico.dto.HistoricoSituacaoResponseDto;
import com.br.ssmup.empresa.historico.entity.HistoricoSituacao;
import com.br.ssmup.empresa.historico.enums.TipoSituacao;
import com.br.ssmup.empresa.historico.mapper.HistoricoSituacaoMapper;
import com.br.ssmup.empresa.historico.repository.HistoricoSituacaoRepository;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.auth.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoSituacaoServiceTest {

    @Mock private HistoricoSituacaoRepository historicoSituacaoRepository;
    @Mock private HistoricoSituacaoMapper historicoSituacaoMapper;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private HistoricoSituacaoService historicoSituacaoService;

    @Test
    @DisplayName("Deve gravar histórico de situação com usuário autenticado")
    void deveGravarHistoricoComUsuarioAutenticado() {
        SecurityContext context = mock(SecurityContext.class);
        var auth = new UsernamePasswordAuthenticationToken("user@test.com", null, Collections.emptyList());
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        historicoSituacaoService.gravarHistoricoSituacao("Motivo teste", empresa, TipoSituacao.INATIVACAO);

        verify(historicoSituacaoRepository).save(any(HistoricoSituacao.class));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve lançar exceção quando não há autenticação")
    void deveLancarExcecaoSemAutenticacao() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> historicoSituacaoService.gravarHistoricoSituacao("Motivo", new Empresa(), TipoSituacao.INATIVACAO))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("Deve listar histórico de situação por empresa")
    void deveListarHistorico() {
        when(historicoSituacaoRepository.findByEmpresaIdOrderByDataDesc(1L)).thenReturn(List.of(new HistoricoSituacao()));
        when(historicoSituacaoMapper.toDto(any())).thenReturn(mock(HistoricoSituacaoResponseDto.class));

        List<HistoricoSituacaoResponseDto> result = historicoSituacaoService.listarHistoricoSituacao(1L);

        assertThat(result).hasSize(1);
        verify(historicoSituacaoRepository).findByEmpresaIdOrderByDataDesc(1L);
    }
}
