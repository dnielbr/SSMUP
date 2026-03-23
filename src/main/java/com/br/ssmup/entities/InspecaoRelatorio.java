package com.br.ssmup.entities;

import com.br.ssmup.enums.StatusInspecao;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
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
@Table(name = "tb_inspecoes_relatorios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InspecaoRelatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, name = "objetivo_inspecao")
    private String objetivoInspecao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String observacoes;

    @Column(nullable = false, name = "data_inspecao")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInspecao;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    StatusInspecao statusInspecao;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToMany
    @JoinTable(
            name = "tb_inspecao_usuario",
            joinColumns = @JoinColumn(name = "inspecao_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuario> usuarios = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;

}

