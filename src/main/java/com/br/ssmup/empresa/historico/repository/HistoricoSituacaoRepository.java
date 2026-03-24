package com.br.ssmup.empresa.historico.repository;

import com.br.ssmup.empresa.historico.entity.HistoricoSituacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoSituacaoRepository extends JpaRepository<HistoricoSituacao, Long> {

    List<HistoricoSituacao> findByEmpresaIdOrderByDataDesc(Long empresaId);

}
