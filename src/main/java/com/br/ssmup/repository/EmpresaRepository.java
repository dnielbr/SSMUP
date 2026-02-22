package com.br.ssmup.repository;

import com.br.ssmup.entities.Empresa;
import com.br.ssmup.enums.RiscoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long>, JpaSpecificationExecutor<Empresa> {
    long countByCnaePrincipalRisco(RiscoSanitario risco);
}
