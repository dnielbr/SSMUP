package com.br.ssmup.empresa.cadastro.entity;
import com.br.ssmup.empresa.cadastro.entity.Empresa;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_localizacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Localizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitude;
    private Double longitude;

    @OneToOne
    @JoinColumn(name = "id_empresa")
    @JsonBackReference
    private Empresa empresa;
}
