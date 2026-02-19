package com.br.ssmup.components;

import com.br.ssmup.exceptions.AuthenticationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${google.client.id}") String clientId){
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(List.of(clientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String token) {
        try {
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                log.error("Token Google inválido");
                throw new AuthenticationException("Token Google inválido");
            }
            log.info("Sucesso em validar o token Google");
            return idToken.getPayload();
        } catch (Exception e) {
            log.error("Erro ao validar o token Google");
            throw new AuthenticationException("Falha ao validar token Google");
        }
    }
}
