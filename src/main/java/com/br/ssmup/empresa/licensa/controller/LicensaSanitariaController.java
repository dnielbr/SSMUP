package com.br.ssmup.empresa.licensa.controller;

import com.br.ssmup.empresa.licensa.dto.LicensaCancelamentoDto;
import com.br.ssmup.empresa.licensa.dto.LicensaReemissaoDto;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaCadastroDto;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaResponseDto;
import com.br.ssmup.empresa.licensa.dto.LicensaFilterDto;
import com.br.ssmup.empresa.licensa.specification.LicensaSanitariaSpecification;
import com.br.ssmup.empresa.licensa.service.LicensaSanitariaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
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

    @GetMapping("pagination/filter")
    public ResponseEntity<Page<LicensaSanitariaResponseDto>> getAllLicensasPageByFilter(
            @ModelAttribute LicensaFilterDto filter,
            @PageableDefault(page = 0, size = 10, sort = "numControle", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var spec = LicensaSanitariaSpecification.buildSpecification(filter);
        return ResponseEntity.ok(licensaSanitariaService.buscarLicensasPageableFilter(spec, pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<LicensaSanitariaResponseDto> getLicensaById(@PathVariable Long id) {
        return ResponseEntity.ok(licensaSanitariaService.buscarLicencaSanitariaById(id));
    }

    @PostMapping("/emitir/{idEmpresa}")
    public ResponseEntity<?> emitirLicensa(
            @PathVariable Long idEmpresa,
            @RequestBody @Valid LicensaSanitariaCadastroDto payload
    ) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alvara_sanitario.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(licensaSanitariaService.emitirAlvara(idEmpresa, payload.numControle()));
    }

    @PostMapping("/reemitir/{idEmpresa}")
    public ResponseEntity<?> reemitirLicensa(
            @PathVariable Long idEmpresa,
            @RequestBody @Valid LicensaReemissaoDto payload
    ) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alvara_sanitario.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(licensaSanitariaService.reemitirLicensa(
                        idEmpresa,
                        payload.numControle(),
                        payload.motivoCancelamento()
                ));
    }

    @PatchMapping("{id}/cancelar")
    public ResponseEntity<LicensaSanitariaResponseDto> cancelarLicensa(
            @PathVariable Long id,
            @RequestBody @Valid LicensaCancelamentoDto payload
    ) {
        return ResponseEntity.ok(licensaSanitariaService.cancelarLicensa(id, payload.motivo()));
    }

    @GetMapping("/imprimir/{idEmpresa}")
    public ResponseEntity<?> imprimirLicensa(@PathVariable Long idEmpresa) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alvara_sanitario.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(licensaSanitariaService.imprimirLicensa(idEmpresa));
    }
}
