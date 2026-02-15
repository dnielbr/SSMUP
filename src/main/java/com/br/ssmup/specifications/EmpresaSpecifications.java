package com.br.ssmup.specifications;

import com.br.ssmup.dto.EmpresaFilterDto;
import com.br.ssmup.entities.Empresa;
import com.br.ssmup.enums.RiscoSanitario;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class EmpresaSpecifications {

    public static Specification<Empresa> byId(Long id) {
        return (root, query, builder) -> {
            if(id!=null){
                return builder.equal(root.get("id"), id);
            }
            return builder.conjunction();
        };
    }

    public static Specification<Empresa> byRazaoSocial(String razaoSocial) {
        return (root, query, builder) -> {
            if(razaoSocial!=null && !razaoSocial.isEmpty()){
                return builder.like(builder.lower(root.get("razaoSocial")), "%"+razaoSocial.toLowerCase()+"%");
            }
            return builder.conjunction();
        };
    }

    public static Specification<Empresa> byNomeFantasia(String nomeFantasia) {
        return (root, query, builder) ->{
            if(nomeFantasia!=null && !nomeFantasia.isEmpty()){
                return builder.like(builder.lower(root.get("nomeFantasia")), "%"+nomeFantasia.toLowerCase()+"%");
            }
            return builder.conjunction();
        };
    }

    public static Specification<Empresa> byCnpj(String cnpj) {
        return (root, query, builder) ->{
            if(cnpj!=null && !cnpj.isEmpty()){
                return builder.like(builder.lower(root.get("cnpj")), "%"+cnpj+"%");
            }
            return builder.conjunction();
        };
    }

    public static Specification<Empresa> byEmail(String email) {
        return (root, query, builder)->{
            if(email!=null && !email.isEmpty()){
                return builder.like(builder.lower(root.get("email")), "%"+email+"%");
            }
            return builder.conjunction();
        };
    }

    public static Specification<Empresa> byInscricaoEstadual(String inscricaoEstadual) {
        return (root, query, builder)->{
            if(inscricaoEstadual!=null && !inscricaoEstadual.isEmpty()){
                return builder.like(builder.lower(root.get("inscricaoEstadual")), "%"+inscricaoEstadual+"%");
            }
            return builder.conjunction();
        };
    }

    public static Specification<Empresa> byAtividadeFirma(String atividadeFirma) {
        return (root, query, builder)->{
            if(atividadeFirma!=null && !atividadeFirma.isEmpty()){
                return builder.like(builder.lower(root.get("atividadeFirma")), "%"+atividadeFirma.toLowerCase()+"%");
            }
            return builder.conjunction();
        };
    }

    public static Specification<Empresa> bySubAtividade(String subAtividade) {
        return (root, query, builder)->{
            if(subAtividade!=null && !subAtividade.isEmpty()){
                return builder.like(builder.lower(root.get("subAtividade")), "%"+subAtividade.toLowerCase()+"%");
            }
            return builder.conjunction();
        };
    }

    public static Specification<Empresa> byDataInicioFuncionamento(LocalDate dataInicioFuncionamento) {
        return (root,query,builder)->{
            if(dataInicioFuncionamento!=null){
                return builder.equal(root.get("dataInicioFuncionamento"), dataInicioFuncionamento);
            }
            return builder.conjunction();
        };
    }

    public static Specification<Empresa> byAtivo(Boolean ativo) {
        return ((root, query, builder) -> {
            if(ativo!=null){
                return builder.equal(root.get("ativo"), ativo);
            }
            return builder.conjunction();
        });
    }

    public static Specification<Empresa> byInspecao(Boolean inspecao) {
        return ((root, query, builder) -> {
            if(inspecao!=null){
                return builder.equal(root.get("inspecao"), inspecao);
            }
            return builder.conjunction();
        });
    }

    public static Specification<Empresa> byRisco(RiscoSanitario risco) {
        return ((root, query, builder)->{
            if(risco!=null){
                return builder.equal(root.get("cnaePrincipal").get("risco"), risco);
            }
            return builder.conjunction();
        });
    }

    public static Specification<Empresa> buildSpecification(EmpresaFilterDto filter) {
        return EmpresaSpecifications.byId(filter.id())
                .and(EmpresaSpecifications.byRazaoSocial(filter.razaoSocial()))
                .and(EmpresaSpecifications.byNomeFantasia(filter.nomeFantasia()))
                .and(EmpresaSpecifications.byCnpj(filter.cnpj()))
                .and(EmpresaSpecifications.byEmail(filter.email()))
                .and(EmpresaSpecifications.byInscricaoEstadual(filter.inscricaoEstadual()))
                .and(EmpresaSpecifications.byAtividadeFirma(filter.atividadeFirma()))
                .and(EmpresaSpecifications.bySubAtividade(filter.subAtividade()))
                .and(EmpresaSpecifications.byDataInicioFuncionamento(filter.dataInicioFuncionamento()))
                .and(EmpresaSpecifications.byAtivo(filter.ativo()))
                .and((EmpresaSpecifications.byInspecao(filter.inspecao())))
                .and((EmpresaSpecifications.byRisco(filter.risco())));
    }
}