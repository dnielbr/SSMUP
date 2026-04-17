package com.br.ssmup.empresa.historico.entity;
import com.br.ssmup.auth.usuario.entity.Usuario;
import com.br.ssmup.core.audit.Auditable;
import com.br.ssmup.empresa.cadastro.entity.Empresa;

import com.br.ssmup.empresa.historico.enums.TipoSituacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_historicos_situacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoSituacao extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String motivo;

    @Column(nullable = false)
    private TipoSituacao tipoSituacao;

    @Column(name = "data", updatable = false)
    private LocalDateTime data;

    @PrePersist
    public void prePersistData() {
        if (data == null) {
            data = LocalDateTime.now();
        }
    }

    @ManyToOne()
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @ManyToOne()
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

}
