package com.br.ssmup.empresa.cnae.entity;

import com.br.ssmup.empresa.cnae.enums.RiscoSanitario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_cnae")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cnae {
    @Id
    private String codigo;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private RiscoSanitario risco;
}
