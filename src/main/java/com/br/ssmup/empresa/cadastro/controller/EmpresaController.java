package com.br.ssmup.empresa.cadastro.controller;

import com.br.ssmup.empresa.cadastro.dto.*;
import com.br.ssmup.empresa.endereco.dto.EnderecoAtualizarDto;
import com.br.ssmup.empresa.endereco.dto.EnderecoResponseDto;
import com.br.ssmup.empresa.endereco.service.EnderecoService;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelAtualizarDto;
import com.br.ssmup.empresa.responsavel.dto.ResponsavelResponseDto;
import com.br.ssmup.empresa.responsavel.service.ResponsavelService;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaCadastroDto;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaResponseDto;
import com.br.ssmup.empresa.licensa.service.LicensaSanitariaService;
import com.br.ssmup.empresa.historico.dto.HistoricoSituacaoRequestDto;
import com.br.ssmup.empresa.historico.dto.HistoricoSituacaoResponseDto;
import com.br.ssmup.empresa.historico.service.HistoricoSituacaoService;
import com.br.ssmup.empresa.cadastro.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("v1/api/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "Ciclo de vida dos estabelecimentos (Cadastro, Inativação, Histórico)")
public class EmpresaController {

    private final EmpresaService empresaService;
    private final EnderecoService enderecoService;
    private final ResponsavelService responsavelService;
    private final LicensaSanitariaService licensaSanitariaService;
    private final HistoricoSituacaoService historicoSituacaoService;

    @GetMapping
    @Operation(summary = "Listar todas", description = "Retorna lista completa de empresas.")
    public ResponseEntity<List<EmpresaResponseDto>> getAllEmpresas() {
        return ResponseEntity.ok().body(empresaService.listarEmpresas());
    }

    @GetMapping("pagination")
    @Operation(summary = "Listar empresas paginadas", description = "Retorna a lista de empresas com paginação.", parameters = {
            @Parameter(name = "page", description = "Número da página (0..N)", example = "0"),
            @Parameter(name = "size", description = "Quantidade de itens por página", example = "10"),
            @Parameter(name = "sort", description = "Ordenação por atributo", example = "razaoSocial")}
    )
    public ResponseEntity<Page<EmpresaResponseDto>> getAllEmpresasPage(
            @Parameter(hidden = true)
            @PageableDefault(page = 0, size = 10, sort = "razaoSocial", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok().body(empresaService.listarEmpresasPageable(pageable));
    }

    @GetMapping("filter")
    @Operation(summary = "Filtro de pesquisa", description = "Busca por Razão Social, CNPJ, Nome Fantasia, Email, Inscrição Estadual, Atividade da Firma, Sub Atividade, Data de inicio de funcionamento, ativa.")
    public ResponseEntity<List<EmpresaResponseDto>> getAllEmpresasByFilter(EmpresaFilterDto filter) {
        return ResponseEntity.ok().body(empresaService.listarEmpresasFilter(filter));
    }

    @GetMapping("pagination/filter")
    @Operation(summary = "Filtro paginado", description = "Busca empresas usando filtros e paginação combinados.", parameters = {
            @Parameter(name = "razaoSocial", description = "Filtra por Razão Social"),
            @Parameter(name = "cnpj", description = "Filtra por CNPJ (apenas números)"),
            @Parameter(name = "nomeFantasia", description = "Filtra por Nome Fantasia"),
            @Parameter(name = "email", description = "Filra por email"),
            @Parameter(name = "inscricaoEstadual", description = "Filtra por inscricao estadual"),
            @Parameter(name = "atividadeFirma", description = "Filtra por atividade da firma"),
            @Parameter(name = "subAtividade", description = "Filtra por sub atividade da firma"),
            @Parameter(name = "dataInicioFuncionamento", description = "Filtra por data de inicio de funcionamento"),
            @Parameter(name = "ativo", description = "Filtra por status de empresa, ativa ou inativa"),
            @Parameter(name = "page", description = "Número da página (0..N)", example = "0"),
            @Parameter(name = "size", description = "Quantidade de itens por página", example = "10"),
            @Parameter(name = "sort", description = "Ordenação por atributo", example = "id")}
    )
    public ResponseEntity<Page<EmpresaResponseDto>> getAllEmpresasPageByFilter(@Parameter(hidden = true) @ModelAttribute EmpresaFilterDto filter, @Parameter(hidden = true) Pageable pageable) {
        log.info("Recebendo requisição de filtro de empresas. Filtros: {}, Página: {}, Size: {}", filter, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok().body(empresaService.listarEmpresasPageableFilter(filter, pageable));
    }

    @GetMapping("ativas")
    @Operation(summary = "Listar Ativas", description = "Filtra apenas empresas com status ativo.")
    public ResponseEntity<List<EmpresaResponseDto>> getAllEmpresasAtivas() {
        return ResponseEntity.ok().body(empresaService.listarEmpresasAtivas());
    }

    @GetMapping("inativas")
    @Operation(summary = "Listar Inativas", description = "Filtra apenas empresas inativadas.")
    public ResponseEntity<List<EmpresaResponseDto>> getAllEmpresasInativas() {
        return ResponseEntity.ok().body(empresaService.listarEmpresasInativas());
    }

    @GetMapping({"{id}"})
    @Operation(summary = "Detalhes da Empresa", description = "Busca uma empresa específica por ID.")
    public ResponseEntity<EmpresaResponseDto> getEmpresas(@PathVariable Long id) {
        return ResponseEntity.ok().body(empresaService.getEmpresaById(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar Empresa", description = "Registra nova empresa, endereco e dados do responsável.")
    public ResponseEntity<EmpresaResponseDto> postEmpresas(@RequestBody @Valid EmpresaCadastroDto payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.saveEmpresa(payload));
    }

    @PutMapping("{id}")
    @Operation(summary = "Editar Empresa", description = "Atualiza dados cadastrais da empresa.")
    public ResponseEntity<EmpresaAtualizarDto> updateEmpresas(@PathVariable Long id, @RequestBody @Valid EmpresaAtualizarDto payload) {
        return ResponseEntity.ok(empresaService.atualizarEmpresa(id, payload));
    }

    @PutMapping("{id}/enderecos")
    @Operation(summary = "Atualizar Endereço", description = "Atualiza especificamente o endereço da empresa.")
    public ResponseEntity<EnderecoResponseDto> updateEndereco(@PathVariable Long id, @RequestBody @Valid EnderecoAtualizarDto payload) {
        return ResponseEntity.ok(enderecoService.atualizarEndereco(id, payload));
    }

    @PutMapping("{id}/responsaveis")
    @Operation(summary = "Atualizar Responsável", description = "Atualiza dados do responsável legal/técnico de uma empresa.")
    public ResponseEntity<ResponsavelResponseDto> updateResponsavel(@PathVariable Long id, @RequestBody @Valid ResponsavelAtualizarDto payload) {
        return ResponseEntity.ok(responsavelService.atualizarResponsavel(id, payload));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletar", description = "Remove o registro do banco. Cuidado: Prefira a inativação.")
    public ResponseEntity<Void> deleteEmpresa(@PathVariable Long id) {
        empresaService.deleteByIdEmpresa(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("{id}/inativar")
    @Operation(summary = "Inativar Empresa", description = "Realiza a exclusão lógica e registra o motivo no histórico.")
    public ResponseEntity<Void> inativarEmpresa(@PathVariable Long id, @RequestBody HistoricoSituacaoRequestDto payload) {
        empresaService.inativarEmpresa(id, payload);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("{id}/ativar")
    @Operation(summary = "Reativar Empresa", description = "Restaura o acesso da empresa e registra no histórico.")
    public ResponseEntity<Void> ativarEmpresa(@PathVariable Long id) {
        empresaService.ativarEmpresa(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("{id}/licensasSanitarias")
    @Operation(summary = "Listar Licenças", description = "Busca todas as licenças emitidas para a empresa.")
    public ResponseEntity<List<LicensaSanitariaResponseDto>> getAllLicensasSanitarias(@PathVariable Long id) {
        return ResponseEntity.ok().body(licensaSanitariaService.listarLicensasSanitariasByEmpresa(id));
    }

    @PostMapping("{id}/licensasSanitarias")
    @Operation(summary = "Gerar Nova Licença", description = "Cria um registro de licença sanitária no sistema para uma empresa.")
    public ResponseEntity<LicensaSanitariaResponseDto> saveLincensaSanitaria(@PathVariable Long id, @RequestBody LicensaSanitariaCadastroDto payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(licensaSanitariaService.saveLicensaSanitariaByEmpresa(id, payload));
    }

    @GetMapping("{id}/historico")
    @Operation(summary = "Consultar Histórico", description = "Linha do tempo de ativações e inativações da empresa.")
    public ResponseEntity<List<HistoricoSituacaoResponseDto>> getAllHistorico(@PathVariable Long id) {
        return ResponseEntity.ok().body(historicoSituacaoService.listarHistoricoSituacao(id));
    }

//    @GetMapping("risco")
//    @Operation(summary = "Consultar quantidades de empresas por risco", description = "Retorna um Json(DTO), contendo as quantidades de empresas de baixo, medio e alto risco")
//    public ResponseEntity<EmpresaRiscoResponseDto> getQtEmpresaRisco() {
//        return ResponseEntity.ok(empresaService.buscarQtEmpresasRisco());
//    }

    @GetMapping("/buscaAproximada")
    @Operation(summary = "Busca Aproximada (Solr)", description = "Busca inteligente com tolerância a erros de digitação.")
    public ResponseEntity<Page<EmpresaResponseDto>> buscarAproximada(
            @RequestParam("termo") String termo,
            @Parameter(hidden = true)
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        if (termo == null || termo.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(empresaService.buscarEmpresasPorText(termo, pageable));
    }

    @PostMapping("/sincronizarSolr")
    public ResponseEntity<String> sincronizarBaseSolr() {
        empresaService.sincronizarBaseComSolr();
        return ResponseEntity.ok("Sincronização iniciada com sucesso! Verifique os logs.");
    }
}
