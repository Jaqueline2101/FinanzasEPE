package com.pe.finanzas.empresa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.pe.finanzas.empresa.dto.VinculoDTO;
import com.pe.finanzas.empresa.entity.Empresa;
import com.pe.finanzas.empresa.entity.EmpresaPersonaVinculo;
import com.pe.finanzas.empresa.repository.EmpresaRepository;
import com.pe.finanzas.empresa.service.EmpresaService;
import com.pe.finanzas.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

        private final EmpresaService empresaService;
        private final EmpresaRepository empresaRepository;

        @PostMapping
        public ResponseEntity<ApiResponse<Empresa>> createEmpresa(@RequestBody Empresa empresa) {
                Empresa saved = empresaService.create(empresa);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<Empresa>builder()
                                                .success(true)
                                                .message("Empresa creada exitosamente")
                                                .data(saved)
                                                .build());
        }

        @GetMapping
        public ResponseEntity<ApiResponse<java.util.List<Empresa>>> getAll() {
                return ResponseEntity.ok(ApiResponse.<java.util.List<Empresa>>builder()
                                .success(true)
                                .message("Empresas obtenidas exitosamente")
                                .data(empresaService.getAll())
                                .build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<Empresa>> getById(@PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.<Empresa>builder()
                                .success(true)
                                .data(empresaService.getById(id))
                                .build());
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<Empresa>> update(
                        @PathVariable Long id,
                        @RequestBody Empresa empresa) {
                return ResponseEntity.ok(ApiResponse.<Empresa>builder()
                                .success(true)
                                .message("Empresa actualizada exitosamente")
                                .data(empresaService.update(id, empresa))
                                .build());
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
                empresaService.delete(id);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Empresa eliminada exitosamente")
                                .build());
        }

        @GetMapping("/por-compania/{id}")
        public ResponseEntity<ApiResponse<java.util.List<Empresa>>> getEmpresasByCompania(@PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.<java.util.List<Empresa>>builder()
                                .success(true)
                                .message("Empresas de la compañía obtenidas exitosamente")
                                .data(empresaService.listarEmpresasPorCompania(id))
                                .build());
        }

        @PostMapping("/vinculo")
        public ResponseEntity<ApiResponse<EmpresaPersonaVinculo>> createVinculo(
                        @Valid @RequestBody VinculoDTO vinculoDTO) {
                EmpresaPersonaVinculo created = empresaService.crearVinculo(vinculoDTO);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<EmpresaPersonaVinculo>builder()
                                                .success(true)
                                                .message("Vínculo Empresa-Personal creado exitosamente")
                                                .data(created)
                                                .build());
        }

        @GetMapping("/{id}/vinculos")
        public ResponseEntity<ApiResponse<java.util.List<EmpresaPersonaVinculo>>> getVinculosByEmpresa(
                        @PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.<java.util.List<EmpresaPersonaVinculo>>builder()
                                .success(true)
                                .message("Vínculos obtenidos exitosamente")
                                .data(empresaService.getVinculosByEmpresa(id))
                                .build());
        }

        @GetMapping("/check-ruc/{ruc}")
        public ResponseEntity<ApiResponse<Boolean>> checkRuc(@PathVariable String ruc) {
                boolean exists = empresaService.checkRucDuplicity(ruc, null);
                return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                                .success(true)
                                .data(exists)
                                .build());
        }
}
