package com.br.ssmup.empresa.cadastro.entity;
import com.br.ssmup.empresa.cnae.entity.Cnae;
import com.br.ssmup.empresa.licensa.entity.LicensaSanitaria;
import com.br.ssmup.empresa.endereco.entity.Endereco;
import com.br.ssmup.empresa.responsavel.entity.Responsavel;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia", nullable = false)
    private String nomeFantasia;

    @Column(nullable = true, unique = true)
    private String cnpj;

    @Column(name = "inscricao_estadual", unique = true)
    private String inscricaoEstadual;

    @Column(name = "atividade_firma", nullable = false)
    private String atividadeFirma;

    @Column(name = "sub_atividade")
    private String subAtividade;

    @Column(name = "data_inicio_funcionamento",  nullable = false)
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioFuncionamento;

    @Column(nullable = false)
    private boolean ativo = true;

//    @Column(nullable = false)
//    private boolean inspecao = false;

    @Email
    @Column(unique = true)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_responsavel", nullable = false)
    @JsonBackReference
    private Responsavel responsavel;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "empresa", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Endereco endereco;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "empresa")
    @JsonManagedReference
    private List<LicensaSanitaria> licensasSanitarias = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "empresa", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Localizacao localizacao;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "cnae_principal_codigo")
//    private Cnae cnaePrincipal;

    public void adicionarEndereco(Endereco endereco) {
        this.endereco = endereco;
        if (endereco != null) {
            endereco.setEmpresa(this);
        }
    }

}
