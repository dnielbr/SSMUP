package com.br.ssmup.controller;

import com.br.ssmup.dto.*;
import com.br.ssmup.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Empresas", description = "Ciclo de vida dos estabelecimentos (Cadastro, Inativação, Histórico)")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    //Listar todas as empresas
    @GetMapping
    @Operation(summary = "Listar todas", description = "Retorna lista completa de empresas.")
    public ResponseEntity<List<EmpresaResponseDto>> getAllEmpresas() {
        return ResponseEntity.ok().body(empresaService.listarEmpresas());
    }

    //Listar as empresas paginadas
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

    //Listar as empresas com filtro
    @GetMapping("filter")
    @Operation(summary = "Filtro de pesquisa", description = "Busca por Razão Social, CNPJ, Nome Fantasia, Email, Inscrição Estadual, Atividade da Firma, Sub Atividade, Data de inicio de funcionamento, ativa.")
    public ResponseEntity<List<EmpresaResponseDto>> getAllEmpresasByFilter(EmpresaFilterDto filter){
        return ResponseEntity.ok().body(empresaService.listarEmpresasFilter(filter));
    }

    //Listar as empresas paginadas com filtro
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
            @Parameter(name = "inspecao", description = "Filtra por inspecao feita ou nao feita"),
            @Parameter(name = "ativo", description = "Filtra por status de empresa, ativa ou inativa"),
            @Parameter(name = "risco", description = "Filtra por risco sanitario, RISCO_I_BAIXO, RISCO_II_MEDIO, RISCO_III_ALTO"),
            @Parameter(name = "page", description = "Número da página (0..N)", example = "0"),
            @Parameter(name = "size", description = "Quantidade de itens por página", example = "10"),
            @Parameter(name = "sort", description = "Ordenação por atributo", example = "id")}
    )
    public ResponseEntity<Page<EmpresaResponseDto>> getAllEmpresasPageByFilter(@Parameter(hidden = true) @ModelAttribute EmpresaFilterDto filter, @Parameter(hidden = true) Pageable pageable) {
        log.info("Recebendo requisição de filtro de empresas. Filtros: {}, Página: {}, Size: {}", filter, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok().body(empresaService.listarEmpresasPageableFilter(filter, pageable));
    }

    //Listar todas as empresas ativas
    @GetMapping("ativas")
    @Operation(summary = "Listar Ativas", description = "Filtra apenas empresas com status ativo.")
    public ResponseEntity<List<EmpresaResponseDto>> getAllEmpresasAtivas() {
        return ResponseEntity.ok().body(empresaService.listarEmpresasAtivas());
    }

    //Listar todas as empresas inativas
    @GetMapping("inativas")
    @Operation(summary = "Listar Inativas", description = "Filtra apenas empresas inativadas.")
    public ResponseEntity<List<EmpresaResponseDto>> getAllEmpresasInativas() {
        return ResponseEntity.ok().body(empresaService.listarEmpresasInativas());
    }

    //Buscar empresa por ID
    @GetMapping({"{id}"})
    @Operation(summary = "Detalhes da Empresa", description = "Busca uma empresa específica por ID.")
    public ResponseEntity<EmpresaResponseDto> getEmpresas(@PathVariable Long id) {
        return ResponseEntity.ok().body(empresaService.getEmpresaById(id));
    }

    //Criar Empresa
    @PostMapping
    @Operation(summary = "Cadastrar Empresa", description = "Registra nova empresa, endereco e dados do responsável.")
    public ResponseEntity<EmpresaResponseDto> postEmpresas(@RequestBody @Valid EmpresaCadastroDto payload){
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.saveEmpresa(payload));
    }

    //Atualizar Empresa por ID
    @PutMapping("{id}")
    @Operation(summary = "Editar Empresa", description = "Atualiza dados cadastrais da empresa.")
    public ResponseEntity<EmpresaAtualizarDto> updateEmpresas(@PathVariable Long id, @RequestBody @Valid EmpresaAtualizarDto payload){
        return ResponseEntity.ok(empresaService.atualizarEmpresa(id, payload));
    }

    //Atulazar o Endereco de uma empresa pelo Id
    @PutMapping("{id}/enderecos")
    @Operation(summary = "Atualizar Endereço", description = "Atualiza especificamente o endereço da empresa.")
    public ResponseEntity<EnderecoResponseDto> updateEndereco(@PathVariable Long id, @RequestBody @Valid EnderecoAtualizarDto payload){
        return ResponseEntity.ok(empresaService.atualizarEndereco(id, payload));
    }

    @PutMapping("{id}/responsaveis")
    @Operation(summary = "Atualizar Responsável", description = "Atualiza dados do responsável legal/técnico de uma empresa.")
    public ResponseEntity<ResponsavelResponseDto> updateResponsavel(@PathVariable Long id, @RequestBody @Valid ResponsavelAtualizarDto payload){
        return ResponseEntity.ok(empresaService.atualizarResponsavel(id, payload));
    }

    //Deletar empresa por id
    @DeleteMapping("{id}")
    @Operation(summary = "Deletar", description = "Remove o registro do banco. Cuidado: Prefira a inativação.")
    public ResponseEntity<Void> deleteEmpresa(@PathVariable Long id){
        empresaService.deleteByIdEmpresa(id);
        return ResponseEntity.noContent().build();
    }

    //Inativar empresa por ID
    @PatchMapping("{id}/inativar")
    @Operation(summary = "Inativar Empresa", description = "Realiza a exclusão lógica e registra o motivo no histórico.")
    public ResponseEntity<Void> inativarEmpresa(@PathVariable Long id, @RequestBody HistoricoSituacaoRequestDto payload){
        empresaService.inativarEmpresa(id, payload);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //Ativar empresa por ID
    @PutMapping ("{id}/ativar")
    @Operation(summary = "Reativar Empresa", description = "Restaura o acesso da empresa e registra no histórico.")
    public ResponseEntity<Void> ativarEmpresa(@PathVariable Long id){
        empresaService.ativarEmpresa(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

//    //Realizar inspeção de Empresa por ID
//    @PutMapping("{id}/realizarInspecao")
//    @Operation(summary = "Registrar Inspeção", description = "Realiza a inspeção de uma empresa, utilizada em empresas de alto risco, para emissão de alvara.")
//    public ResponseEntity<Void> realizarInspecao(@PathVariable Long id){
//        empresaService.realizarInspecao(id);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//    }

    //Buscar licensas sanitarias de uma empresa pelo ID
    @GetMapping("{id}/licensasSanitarias")
    @Operation(summary = "Listar Licenças", description = "Busca todas as licenças emitidas para a empresa.")
    public ResponseEntity<List<LicensaSanitariaResponseDto>> getAllLicensasSanitarias(@PathVariable Long id){
        return ResponseEntity.ok().body(empresaService.listarLicensasSanitarias(id));
    }

    //Criar licensa sanitaria para uma empresa
    @PostMapping("{id}/licensasSanitarias")
    @Operation(summary = "Gerar Nova Licença", description = "Cria um registro de licença sanitária no sistema para uma empresa.")
    public ResponseEntity<LicensaSanitariaResponseDto> saveLincensaSanitaria(@PathVariable Long id, @RequestBody LicensaSanitariaCadastroDto payload){
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.saveLicensaSanitaria(id,payload));
    }

    //Listar hitorico de situacoes da empresa, ativacao e inativacao.
    @GetMapping("{id}/historico")
    @Operation(summary = "Consultar Histórico", description = "Linha do tempo de ativações e inativações da empresa.")
    public ResponseEntity<List<HistoricoSituacaoResponseDto>> getAllHistorico(@PathVariable Long id){
        return ResponseEntity.ok().body(empresaService.listarHistoricoSituacao(id));
    }

    //Listar quantidades de empresas por risco, baixo, medio e alto
    @GetMapping("risco")
    @Operation(summary = "Consultar quantidades de empresas por risco", description = "Retorna um Json(DTO), contendo as quantidades de empresas de baixo, medio e alto risco")
    public ResponseEntity<EmpresaRiscoResponseDto> getQtEmpresaRisco(){
        return ResponseEntity.ok(empresaService.buscarQtEmpresasRisco());
    }

    @GetMapping("/buscaAproximada")
    @Operation(summary = "Busca Aproximada (Solr)", description = "Busca inteligente com tolerância a erros de digitação.")
    public ResponseEntity<Page<EmpresaResponseDto>> buscarAproximada(
            @RequestParam("termo") String termo,
            @Parameter(hidden = true)
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        if(termo == null || termo.isBlank()){
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
