package com.br.ssmup.service;

import com.br.ssmup.dto.EmpresaSolrDto;
import com.br.ssmup.entities.Empresa;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmpresaSolrService {

    private final SolrClient solrClient;
    private final String COLLECTION = "empresas";

    public EmpresaSolrService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public void salvarNoSolr(Empresa empresa) {
        try {
            solrClient.addBean(COLLECTION, new EmpresaSolrDto(empresa.getId().toString(), empresa.getRazaoSocial(), empresa.getNomeFantasia()));
            solrClient.commit(COLLECTION);
            log.info("Empresa indexada no Solr: ID {}", empresa.getId());
        } catch (Exception e) {
            log.error("Erro ao salvar empresa no Solr (ID: {}): {}", empresa.getId(), e.getMessage());
        }
    }

    public void removerDoSolr(Long id){
        try {
            solrClient.deleteById(COLLECTION, id.toString());
            solrClient.commit(COLLECTION);
            log.info("Empresa removida no Solr: ID {}", id);
        } catch (Exception e) {
            log.error("Erro ao remover empresa do Solr (ID: {}): {}", id, e.getMessage());
        }
    }

    public Page<EmpresaSolrDto> buscarAproximada(String termo, Pageable pageable){
        try {
            SolrQuery solrQuery = new SolrQuery();

            String[] palavras = termo.trim().split("\\s+");
            StringBuilder queryBuilder = new StringBuilder();

            for (int i = 0; i < palavras.length; i++) {
                String p = palavras[i];
                queryBuilder.append(String.format(
                        "(razao_social:*%s* OR nome_fantasia:*%s* OR razao_social:%s~2 OR nome_fantasia:%s~2)",
                        p, p, p, p
                ));
                if (i < palavras.length - 1) {
                    queryBuilder.append(" AND ");
                }
            }

            solrQuery.setQuery(queryBuilder.toString());

            solrQuery.setStart((int) pageable.getOffset());
            solrQuery.setRows(pageable.getPageSize());

            QueryResponse response = solrClient.query(COLLECTION, solrQuery);
            List<EmpresaSolrDto> beans = response.getBeans(EmpresaSolrDto.class);

            long totalElementos = response.getResults().getNumFound();

            return new PageImpl<>(beans, pageable, totalElementos);

        } catch (Exception e) {
            log.error("Erro ao realizar busca no Solr pelo termo '{}':", termo, e);
            return Page.empty(pageable);
        }
    }

    public void sincronizarEmLote(List<EmpresaSolrDto> documentos){
        if(documentos == null || documentos.isEmpty()) return;
        try {
            solrClient.deleteByQuery(COLLECTION, "*:*");
            solrClient.addBeans(COLLECTION, documentos);
            solrClient.commit(COLLECTION);
            log.info("Sincronização em lote concluída! {} registros indexados.", documentos.size());
        } catch (Exception e) {
            log.error("Erro na sincronização em lote com o Solr: ", e);
        }
    }

}
