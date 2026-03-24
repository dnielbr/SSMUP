# Guia Completo de Melhorias - JWT e Refresh Token (Spring Boot)

Este documento descreve, de forma estruturada e passo a passo, as melhorias necessárias para evoluir a implementação atual de autenticação com JWT e Refresh Token para um padrão seguro e adequado para produção.

---

# 1. Objetivo

Garantir:

- Segurança no uso de tokens
- Controle de sessão sem estado (stateless)
- Proteção contra uso indevido de tokens
- Fluxo correto de renovação de autenticação

---

# 2. Correção do Filtro de Segurança (CRÍTICO)

## Problema
O filtro atual ignora erros de validação do token, permitindo que requisições inválidas continuem.

## Ação necessária
Interromper imediatamente a requisição quando o token for inválido.

## Implementação

Substituir:

```java
catch (Exception ignored) {
    System.out.println("Erro ao validar token no filtro: " + ignored.getMessage());
}
```

Por:

```java
catch (Exception ex) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return;
}
```

---

# 3. Validação de Token Revogado

## Problema
O campo `revoked` existe, mas não está sendo considerado na validação.

## Ação necessária
Validar se o token foi revogado antes de aceitá-lo.

## Implementação

```java
public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().isBefore(Instant.now()) || token.isRevoked()) {
        refreshTokenRepository.delete(token);
        throw new UnauthorizedException("Refresh token inválido ou expirado.");
    }
    return token;
}
```

---

# 4. Implementação de Rotação de Refresh Token

## Problema
O refresh token atual pode ser reutilizado até expirar.

## Risco
Se comprometido, permite acesso prolongado.

## Ação necessária
Implementar rotação de refresh token.

## Fluxo correto

1. Receber refresh token
2. Validar token
3. Remover token antigo
4. Gerar novo refresh token
5. Gerar novo access token

## Implementação

```java
public TokenResponse refresh(String oldToken) {
    RefreshToken token = refreshTokenService.findByToken(oldToken);
    refreshTokenService.verifyExpiration(token);

    Usuario usuario = token.getUsuario();

    refreshTokenRepository.delete(token);

    RefreshToken newRefresh = refreshTokenService.createRefreshToken(usuario);

    String newAccess = tokenService.generateAccessToken(usuario);

    return new TokenResponse(newAccess, newRefresh.getToken());
}
```

---

# 5. Uso de HttpOnly Cookie para Refresh Token

## Problema
O envio do refresh token no corpo da requisição expõe risco de XSS.

## Ação necessária
Enviar o refresh token via cookie seguro.

## Implementação

Header de resposta:

```
Set-Cookie: refreshToken=valor; HttpOnly; Secure; SameSite=Strict
```

---

# 6. Correção de Paths no SecurityConfig

## Problema
Paths sem barra inicial podem não ser corretamente interpretados.

## Ação necessária
Adicionar barra inicial em todos os endpoints.

## Implementação

Corrigir:

```java
requestMatchers("v1/api/usuarios/**")
```

Para:

```java
requestMatchers("/v1/api/usuarios/**")
```

---

# 7. Tratamento Seguro de Roles

## Problema
Possível erro ao acessar role inexistente no token.

## Ação necessária
Validar antes de criar authorities.

## Implementação

```java
String role = claims.get("role", String.class);

List<SimpleGrantedAuthority> authorities = role != null
    ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
    : List.of();
```

---

# 8. Fluxo Completo de Autenticação

## 8.1 Login

Retorna:

```json
{
  "accessToken": "valor",
  "refreshToken": "valor"
}
```

## 8.2 Requisições autenticadas

Header:

```
Authorization: Bearer <access_token>
```

## 8.3 Expiração do Access Token

- API responde com status 401

## 8.4 Renovação

Requisição:

```
POST /auth/refresh
```

## 8.5 Resposta

```json
{
  "accessToken": "novo_valor",
  "refreshToken": "novo_valor"
}
```

---

# 9. Boas Práticas

- Access token com duração curta (ex: 15 minutos)
- Refresh token com duração maior (ex: 7 dias)
- Rotacionar refresh token a cada uso
- Invalidar tokens no logout
- Utilizar HttpOnly cookie para refresh token
- Evitar armazenamento em localStorage

---

# 10. Melhorias Futuras

- Implementar blacklist de tokens
- Auditoria de autenticação
- Controle por dispositivo
- Limitação de requisições no endpoint de refresh

---

# 11. Conclusão

Seguindo este guia, a aplicação evolui para um padrão seguro, reduzindo riscos de vazamento de token e melhorando o controle de autenticação em ambientes de produção.

