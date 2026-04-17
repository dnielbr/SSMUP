package com.br.ssmup.empresa.cadastro.repository;

import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.cnae.enums.RiscoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long>, JpaSpecificationExecutor<Empresa> {
//    long countByCnaePrincipalRisco(RiscoSanitario risco);
    List<Empresa> findByAtivo(boolean ativo);

    @Query("SELECT MONTH(e.criadoEm) AS mes, COUNT(e) AS quantidade " +
           "FROM Empresa e " +
           "WHERE YEAR(e.criadoEm) = :ano " +
           "GROUP BY MONTH(e.criadoEm) " +
           "ORDER BY MONTH(e.criadoEm)")
    List<Object[]> contarCadastrosPorMes(@Param("ano") int ano);
}
