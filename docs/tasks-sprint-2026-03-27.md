# SSMUP Features Implementation - Sprint 27/03/2026

## 1. Painel - Empresas Cadastradas por Mês
- [ ] Backend: Add `createdAt`/`updatedAt` audit fields to `Empresa` entity
- [ ] Backend: Create `EmpresaRepository.countByMonth()` query (using `createdAt`)
- [ ] Backend: Create DTO for monthly count response
- [ ] Backend: Add endpoint `GET /empresas/cadastros-mensais` in `EmpresaController`
- [ ] Backend: Add service method in `EmpresaService`
- [ ] Frontend: Create `empresasMensaisService.ts`
- [ ] Frontend: Update `usePainelData` hook to fetch monthly data
- [ ] Frontend: Update `CustomBarChart` to use dynamic data from backend

## 2. Página de Licenças Sanitárias
- [ ] Frontend: Create `ILicensaSanitaria.ts` interface
- [ ] Frontend: Create `licensaService.ts` for API calls
- [ ] Frontend: Create `LicensasPage.tsx` page with table listing
- [ ] Frontend: Create `DetailsLicensaPage.tsx` with license details
- [ ] Frontend: Add print license button (re-download existing PDF)
- [ ] Frontend: Add routes in `Routes.tsx`
- [ ] Frontend: Add navigation link in sidebar/navbar

## 3. Refatorar Emissão de Licença (Número de Controle Manual)
- [ ] Backend: Refactor `emitirAlvara()` to accept `numControle` from request body
- [ ] Backend: Add validation for duplicate `numControle`
- [ ] Backend: Add proper exception handling for all edge cases
- [ ] Backend: Add `DuplicateResourceException` for duplicate control numbers
- [ ] Backend: Update `GlobalExceptionHandler` with new exception handler
- [ ] Frontend: Create `ModalEmitirLicensa.tsx` modal for control number input
- [ ] Frontend: Update `EmpresaActionsButton` to open modal instead of direct download
- [ ] Frontend: Update `UpdateEmpresaContext` to handle the new flow

## 4. Testes
- [ ] Backend: Update `LicensaSanitariaServiceTest` for refactored `emitirAlvara`
- [ ] Backend: Add test for monthly count endpoint in `EmpresaServiceTest`
- [ ] Backend: Add tests for exception scenarios (duplicate numControle, empresa not found, etc.)
- [ ] Run all existing tests to verify no regressions
