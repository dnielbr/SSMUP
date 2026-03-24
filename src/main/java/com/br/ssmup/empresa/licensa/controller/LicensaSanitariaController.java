package com.br.ssmup.empresa.licensa.controller;

import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaResponseDto;
import com.br.ssmup.empresa.licensa.service.LicensaSanitariaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/api/licensas")
public class LicensaSanitariaController {

    private final LicensaSanitariaService licensaSanitariaService;

    public LicensaSanitariaController(LicensaSanitariaService licensaSanitariaService) {
        this.licensaSanitariaService = licensaSanitariaService;
    }

    @GetMapping
    public ResponseEntity<List<LicensaSanitariaResponseDto>> getAllLicensas(){
        return ResponseEntity.ok(licensaSanitariaService.buscarLicensasSanitaria());
    }

    @GetMapping("pagination")
    public ResponseEntity<Page<LicensaSanitariaResponseDto>> getAllLicensasPage(@PageableDefault(page = 0, size = 10, sort = "numControle", direction = Sort.Direction.ASC) Pageable pageable){
        return ResponseEntity.ok(licensaSanitariaService.buscarLicensasSanitariaPagable(pageable));
    }

    @PostMapping("/emitir/{idEmpresa}")
    public ResponseEntity<?> emitirLicensa(@PathVariable Long idEmpresa) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alvara_sanitario.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(licensaSanitariaService.emitirAlvara(idEmpresa));
    }

}
