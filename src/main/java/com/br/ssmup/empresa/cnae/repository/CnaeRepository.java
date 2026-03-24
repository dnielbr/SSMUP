package com.br.ssmup.empresa.cnae.repository;

import com.br.ssmup.empresa.cnae.entity.Cnae;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CnaeRepository extends JpaRepository<Cnae, String> {
    Optional<Cnae> findByCodigo(String codigo);

    //procura tanto no código (ex: "5611") quanto na descrição (ex: "Restaurante")
    @Query("SELECT c FROM Cnae c WHERE LOWER(c.codigo) LIKE LOWER(CONCAT('%', :busca, '%')) OR LOWER(c.descricao) LIKE LOWER(CONCAT('%', :busca, '%'))")
    List<Cnae> buscarPorCodigoOuDescricao(@Param("busca") String busca);
}
