package com.br.ssmup.empresa.licensa.repository;

import com.br.ssmup.empresa.licensa.entity.LicensaSanitaria;
import com.br.ssmup.empresa.licensa.enums.StatusLicensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LicensaSanitariaRepository extends JpaRepository<LicensaSanitaria, Long>, JpaSpecificationExecutor<LicensaSanitaria> {
       Optional<LicensaSanitaria> findByNumControle(String numControle);
       boolean existsByEmpresaIdAndStatus(Long empresaId, StatusLicensa status);
       Optional<LicensaSanitaria> findFirstByEmpresaIdAndStatus(Long empresaId, StatusLicensa status);
}
