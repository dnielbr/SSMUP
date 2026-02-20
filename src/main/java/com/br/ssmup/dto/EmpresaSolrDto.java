package com.br.ssmup.dto;

import org.apache.solr.client.solrj.beans.Field;
import java.util.Collection;

public class EmpresaSolrDto {

    private String id;
    private String razaoSocial;
    private String nomeFantasia;

    public EmpresaSolrDto() {}

    public EmpresaSolrDto(String id, String razaoSocial, String nomeFantasia) {
        this.id = id;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
    }

    public String getId() { return id; }

    @Field("id")
    public void setId(String id) { this.id = id; }

    public String getRazaoSocial() { return razaoSocial; }

    @Field("razao_social")
    public void setRazaoSocial(Object razaoSocial) {
        this.razaoSocial = converterParaString(razaoSocial);
    }

    public String getNomeFantasia() { return nomeFantasia; }

    @Field("nome_fantasia")
    public void setNomeFantasia(Object nomeFantasia) {
        this.nomeFantasia = converterParaString(nomeFantasia);
    }

    private String converterParaString(Object valor) {
        return switch (valor) {
            case null -> null;
            case Collection<?> c when !c.isEmpty() -> c.iterator().next().toString();
            case Collection<?> c -> null;
            case Object[] a when a.length > 0 -> a[0].toString();
            case Object[] a -> null;
            default -> valor.toString();
        };
    }
}