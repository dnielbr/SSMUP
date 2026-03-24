# Phase 7 — Refatoração para Macro-Módulos (Bounded Contexts)

Este documento descreve o passo a passo da migração da arquitetura atual (Package-by-Feature fletado) para a nova arquitetura de **Macro-Módulos (Monolito Modular)**.

## 🎯 Objetivo
Agrupar os módulos de negócios em contextos fechados para que desenvolvedores tenham visibilidade clara do escopo do domínio, preparando o sistema para o futuro módulo de `Caminhão Pipa`. Tudo será exaustivamente validado através da suíte de 45 testes unitários.

## 🏗️ Estrutura a ser criada

1.  **Macro-Módulo de Acesso (`auth`)**
    *   `auth` (Mantém controladores e serviços de autenticação).
    *   `usuario` (Movido para dentro de `auth/usuario`).

2.  **Macro-Módulo de Empresa (`empresa`)**
    *   `cadastro` (As antigas classes root da `empresa` serão encapsuladas aqui).
    *   `cnae` (Movido para `empresa/cnae`).
    *   `endereco` (Movido para `empresa/endereco`).
    *   `historico` (Movido para `empresa/historico`).
    *   `inspecao` (Movido para `empresa/inspecao`).
    *   `licensa` (Movido para `empresa/licenca`).
    *   `responsavel` (Movido para `empresa/responsavel`).

## 🛠️ Passo a Passo da Execução

1.  **Cópia de Segurança Sensível**
    *   Mover temporariamente o conteúdo atual de `empresa/` para evitar colisão na criação das novas subpastas.
2.  **Refatoração do pacote `auth` (Acesso)**
    *   Mover a pasta `usuario/` (main e test) para dentro de `auth/`.
    *   Buscar/Substituir: `package com.br.ssmup.usuario` -> `package com.br.ssmup.auth.usuario`.
    *   Buscar/Substituir: `import com.br.ssmup.usuario` -> `import com.br.ssmup.auth.usuario`.
3.  **Refatoração do pacote `empresa` (Core do Negócio)**
    *   Mover classes da empresa (`controller`, `service`, `entity`, etc.) para `empresa/cadastro/`.
    *   Atualizar todos os imports referenciando `com.br.ssmup.empresa.[camada]` para `com.br.ssmup.empresa.cadastro.[camada]`.
    *   Mover as pastas `cnae`, `endereco`, `historico`, `inspecao`, `licensa` e `responsavel` para dentro da raiz de `empresa/`.
    *   Atualizar os imports respectivos inserindo `.empresa.` no caminho dos pacotes.
4.  **Validação Automática Rigorosa (`mvn clean test`)**
    *   Garantir que os 89 arquivos compilam perfeitamente.
    *   Garantir que todos os 45 testes unitários Mockito rodam e passam com sucesso verde na plataforma.
