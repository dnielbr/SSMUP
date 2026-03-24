package com.br.ssmup.core.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SaniMup API - Sistema Sanitário Municipal Unificado de Processos")
                        .description("""
                                ## Documentação Técnica Oficial - PISI/TCC
                                API RESTful desenvolvida para o **Sistema Sanitário Municipal Unificado de Processos (SaniMup)**.
                                O sistema tem como objetivo digitalizar e otimizar os processos administrativos da Vigilância Sanitária municipal.
                                Trabalho de Conclusão de Curso (TCC) do curso de **Tecnologia em Sistemas para Internet** do **IFPB - Campus Guarabira**.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Everaldo Daniel & Jefferson Almir")
                                .email("suport@sanimup.edu.br")
                                .url("https://github.com/dnielbr/SSMUP"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Ambiente de Desenvolvimento (Local)")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT obtido no login.")));
    }
}
