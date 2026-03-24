package com.br.ssmup.empresa.responsavel.entity;
import com.br.ssmup.empresa.cadastro.entity.Empresa;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_responsaveis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Responsavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String rg;

    @Column(nullable = true)
    private String escolaridade;

    @Column(nullable = true)
    private String formacao;

    @Column(nullable = true)
    private String especializacao;

    @Column(name = "registro_conselho", nullable = true)
    private String registroConselho;

    @OneToMany(mappedBy = "responsavel")
    @JsonManagedReference
    private List<Empresa> empresas = new ArrayList<>();

}
