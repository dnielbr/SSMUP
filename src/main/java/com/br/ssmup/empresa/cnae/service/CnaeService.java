package com.br.ssmup.empresa.cnae.service;

import com.br.ssmup.empresa.cnae.entity.Cnae;
import com.br.ssmup.empresa.cnae.repository.CnaeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class CnaeService {
    private final CnaeRepository cnaeRepository;

    public CnaeService(CnaeRepository cnaeRepository) {
        this.cnaeRepository = cnaeRepository;
    }

    @Transactional
    public String popularBancoDeDados() {
        ObjectMapper mapper = new ObjectMapper();

        TypeReference<List<Cnae>> typeReference = new TypeReference<List<Cnae>>(){};
        InputStream inputStream = TypeReference.class.getResourceAsStream("/data/cnaes.json");

        try {
            List<Cnae> cnaes = mapper.readValue(inputStream, typeReference);

            cnaeRepository.saveAll(cnaes);

            return "Sucesso! " + cnaes.size() + " CNAEs foram importados.";
        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler o arquivo JSON: " + e.getMessage());
        }
    }

    public List<Cnae> listarTodos() {
        return cnaeRepository.findAll();
    }
}
