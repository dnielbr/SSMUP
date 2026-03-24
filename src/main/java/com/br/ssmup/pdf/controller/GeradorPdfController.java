package com.br.ssmup.pdf.controller;

import com.br.ssmup.empresa.cadastro.dto.EmpresaResponseDto;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaResponseDto;
import com.br.ssmup.empresa.cadastro.service.EmpresaService;
import com.br.ssmup.pdf.service.GeradorPdfService;
import com.br.ssmup.empresa.licensa.service.LicensaSanitariaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/api")
public class GeradorPdfController {

    private final GeradorPdfService geradorPdfService;
    private final LicensaSanitariaService licencaService;
    private final EmpresaService empresaService;

    public GeradorPdfController(GeradorPdfService geradorPdfService, LicensaSanitariaService licencaService, EmpresaService empresaService) {
        this.geradorPdfService = geradorPdfService;
        this.licencaService = licencaService;
        this.empresaService = empresaService;
    }

    @GetMapping("/licenca/pdf")
    public ResponseEntity<byte[]> getlicensaPdf(@RequestParam String numControl, @RequestParam Long idEmpresa){
        LicensaSanitariaResponseDto licenca = licencaService.buscarLicencaSanitariaByNumControle(numControl);
        EmpresaResponseDto empresa = empresaService.getEmpresaById(idEmpresa);
        byte[] pdfBytes = geradorPdfService.gerarLicensaSanitariaPdf(empresa,licenca);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
