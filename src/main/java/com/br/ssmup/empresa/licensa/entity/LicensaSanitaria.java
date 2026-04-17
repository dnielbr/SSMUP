package com.br.ssmup.empresa.licensa.entity;
import com.br.ssmup.core.audit.Auditable;
import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.licensa.enums.StatusLicensa;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_licensas_sanitarias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicensaSanitaria extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "num_controle", nullable = false)
    private String numControle;

    @Column(name = "data_emissao", updatable = false, nullable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataEmissao;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @Column(name = "data_validade", nullable = false)
    private LocalDate dataValidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLicensa status;

    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    @Column(name = "licensa_substituta_id")
    private Long licensaSubstitutaId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_empresa")
    @JsonBackReference
    private Empresa empresa;

    @PrePersist
    public void prePersist(){
        this.dataEmissao = LocalDateTime.now();
        this.dataValidade = LocalDate.now().plusYears(1);
        if (this.status == null) {
            this.status = StatusLicensa.ATIVA;
        }
    }

    @PreUpdate
    public void preUpdate(){
        if(this.status == StatusLicensa.ATIVA && LocalDate.now().isAfter(this.dataValidade)){
            this.status = StatusLicensa.VENCIDA;
        }
    }
}
