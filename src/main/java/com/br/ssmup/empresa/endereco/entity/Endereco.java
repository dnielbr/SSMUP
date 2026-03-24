package com.br.ssmup.empresa.endereco.entity;
import com.br.ssmup.empresa.cadastro.entity.Empresa;

import com.br.ssmup.empresa.endereco.enums.UnidadeFederativa;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_enderecos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_empresa")
    @JsonBackReference
    private Empresa empresa;

    @Column(nullable = false)
    private String rua;

    @Column(nullable = true)
    private String numero;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private String cep;

    @Column(nullable = false)
    private String municipio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnidadeFederativa uf;

    @Column(nullable = true)
    private String telefone;

}