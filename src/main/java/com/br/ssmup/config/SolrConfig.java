package com.br.ssmup.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class SolrConfig {

    @Value("${spring.data.solr.host}")
    private String url;

    @Bean
    public SolrClient solrClient() {
        return new HttpJdkSolrClient.Builder(url)
                .withConnectionTimeout(10000, TimeUnit.MILLISECONDS)
                .withIdleTimeout(60000, TimeUnit.MILLISECONDS)
                .build();
    }
}
