package com.br.ssmup.empresa.responsavel.controller;

import com.br.ssmup.empresa.cadastro.dto.EmpresaResponseDto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelCadastroDto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelResponseDto;
import com.br.ssmup.empresa.responsavel.service.ResponsavelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/api/responsaveis")
public class ResponsavelController {

    private final ResponsavelService responsavelService;

    public ResponsavelController(ResponsavelService responsavelService) {
        this.responsavelService = responsavelService;
    }

    @PostMapping
    public ResponseEntity<ResponsavelResponseDto> salvarResponsavel(@RequestBody @Valid ResponsavelCadastroDto payload){
        return ResponseEntity.status(HttpStatus.CREATED).body(responsavelService.salvar(payload));
    }

    @GetMapping
    public ResponseEntity<List<ResponsavelResponseDto>> listarResponsaveis(){
        return ResponseEntity.ok().body(responsavelService.listarResponsaveis());
    }

    @GetMapping("{id}/empresas")
    public ResponseEntity<List<EmpresaResponseDto>> listarEmpresasResponsaveis(@PathVariable Long id){
        return ResponseEntity.ok().body(responsavelService.listarEmpresasResponsaveis(id));
    }

    @GetMapping({"{id}"})
    public ResponseEntity<ResponsavelResponseDto> buscarResponsavelById(@PathVariable Long id){
        return ResponseEntity.ok().body(responsavelService.buscarResponsavelById(id));
    }

    @GetMapping("cpf/{cpf}")
    public ResponseEntity<ResponsavelResponseDto> buscarResponsavelByCpf(@PathVariable @Valid String cpf){
        return ResponseEntity.ok().body(responsavelService.buscarResponsavelByCpf(cpf));
    }
}
