package com.br.ssmup.empresa.licensa.specification;

import com.br.ssmup.empresa.cadastro.entity.Empresa;
import com.br.ssmup.empresa.licensa.dto.LicensaFilterDto;
import com.br.ssmup.empresa.licensa.entity.LicensaSanitaria;
import com.br.ssmup.empresa.licensa.enums.StatusLicensa;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class LicensaSanitariaSpecification {

    public static Specification<LicensaSanitaria> byNumControle(String numControle) {
        return (root, query, builder) -> {
            if (numControle != null && !numControle.isEmpty()) {
                return builder.like(builder.lower(root.get("numControle")), "%" + numControle.toLowerCase() + "%");
            }
            return builder.conjunction();
        };
    }

    public static Specification<LicensaSanitaria> byCnpj(String cnpj) {
        return (root, query, builder) -> {
            if (cnpj != null && !cnpj.isEmpty()) {
                Join<LicensaSanitaria, Empresa> empresaJoin = root.join("empresa", JoinType.INNER);
                return builder.like(builder.lower(empresaJoin.get("cnpj")), "%" + cnpj + "%");
            }
            return builder.conjunction();
        };
    }

    public static Specification<LicensaSanitaria> byStatus(StatusLicensa status) {
        return (root, query, builder) -> {
            if (status != null) {
                return builder.equal(root.get("status"), status);
            }
            return builder.conjunction();
        };
    }

    public static Specification<LicensaSanitaria> buildSpecification(LicensaFilterDto filter) {
        return Specification.where(byNumControle(filter.numControle()))
                .and(byCnpj(filter.cnpj()))
                .and(byStatus(filter.status()));
    }
}
