package com.br.ssmup.empresa.cnae.controller;

import com.br.ssmup.empresa.cnae.entity.Cnae;
import com.br.ssmup.empresa.cnae.repository.CnaeRepository;
import com.br.ssmup.empresa.cnae.service.CnaeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/api/cnaes")
public class CnaeController {
    private final CnaeRepository cnaeRepository;
    private final CnaeService cnaeService; // Novo

    public CnaeController(CnaeRepository cnaeRepository, CnaeService cnaeService) {
        this.cnaeRepository = cnaeRepository;
        this.cnaeService = cnaeService;
    }

    @GetMapping
    public List<Cnae> listar(@RequestParam(required = false) String busca) {
        if (busca != null && !busca.isBlank()) {
            return cnaeRepository.buscarPorCodigoOuDescricao(busca);
        }
        return cnaeRepository.findAll();
    }

    @PostMapping("/popular")
    public ResponseEntity<String> popularBanco() {
        try {
            String mensagem = cnaeService.popularBancoDeDados();
            return ResponseEntity.ok(mensagem);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
