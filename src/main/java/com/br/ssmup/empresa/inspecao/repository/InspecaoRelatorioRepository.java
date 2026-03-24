package com.br.ssmup.empresa.inspecao.repository;

import com.br.ssmup.empresa.inspecao.entity.InspecaoRelatorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspecaoRelatorioRepository extends JpaRepository<InspecaoRelatorio, Long> {
    List<InspecaoRelatorio> findAllByEmpresaId(Long empresaId);
}
