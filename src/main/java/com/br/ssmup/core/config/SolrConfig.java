package com.br.ssmup.core.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrConfig {

    @Value("${spring.data.solr.host}")
    private String url;

    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder(url)
                .withConnectionTimeout(5000)
                .withSocketTimeout(10000)
                .build();
    }
}
