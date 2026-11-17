package com.pe.finanzas.paquetes.controller;

import com.pe.finanzas.paquetes.dto.PaqueteDto;
import com.pe.finanzas.paquetes.dto.PaqueteRequest;
import com.pe.finanzas.paquetes.service.PaqueteService;
import com.pe.finanzas.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de Paquetes
 * 
 * PERMISOS:
 * - PAQUETE_CREAR: Solo ADMIN
 * - PAQUETE_LEER: ADMIN y CAJERO
 * - PAQUETE_ACTUALIZAR: Solo ADMIN
 * - PAQUETE_ELIMINAR: Solo ADMIN
 */
@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaqueteController {

        private final PaqueteService paqueteService;

        /**
         * Crear nuevo paquete
         * Solo ADMIN
         */
        @PostMapping
        @PreAuthorize("hasAuthority('PAQUETE_CREAR')")
        public ResponseEntity<ApiResponse<PaqueteDto>> crearPaquete(
                        @Valid @RequestBody PaqueteRequest request) {

                PaqueteDto paquete = paqueteService.crearPaquete(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<PaqueteDto>builder()
                                                .success(true)
                                                .message("Paquete creado exitosamente")
                                                .data(paquete)
                                                .build());
        }

        /**
         * Actualizar paquete existente
         * Solo ADMIN
         */
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('PAQUETE_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<PaqueteDto>> actualizarPaquete(
                        @PathVariable Long id,
                        @Valid @RequestBody PaqueteRequest request) {

                PaqueteDto paquete = paqueteService.actualizarPaquete(id, request);

                return ResponseEntity.ok(ApiResponse.<PaqueteDto>builder()
                                .success(true)
                                .message("Paquete actualizado exitosamente")
                                .data(paquete)
                                .build());
        }

        /**
         * Obtener paquete por ID
         * ADMIN y CAJERO
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('PAQUETE_LEER')")
        public ResponseEntity<ApiResponse<PaqueteDto>> obtenerPaquete(@PathVariable Long id) {

                PaqueteDto paquete = paqueteService.obtenerPaquete(id);

                return ResponseEntity.ok(ApiResponse.<PaqueteDto>builder()
                                .success(true)
                                .message("Paquete obtenido exitosamente")
                                .data(paquete)
                                .build());
        }

        /**
         * Listar todos los paquetes (paginado)
         * ADMIN y CAJERO
         */
        @GetMapping
        @PreAuthorize("hasAuthority('PAQUETE_LEER')")
        public ResponseEntity<ApiResponse<Page<PaqueteDto>>> listarPaquetes(
                        @RequestParam(required = false) String categoria,
                        @PageableDefault(page = 0, size = 20, sort = "nombre") Pageable pageable) {

                Page<PaqueteDto> paquetes = paqueteService.listarPaquetesPorCategoria(categoria, pageable);

                return ResponseEntity.ok(ApiResponse.<Page<PaqueteDto>>builder()
                                .success(true)
                                .message("Paquetes obtenidos exitosamente")
                                .data(paquetes)
                                .build());
        }

        /**
         * Listar paquetes activos
         * ADMIN y CAJERO
         */
        @GetMapping("/activos")
        @PreAuthorize("hasAuthority('PAQUETE_LEER')")
        public ResponseEntity<ApiResponse<List<PaqueteDto>>> listarPaquetesActivos() {

                List<PaqueteDto> paquetes = paqueteService.listarPaquetesActivos();

                return ResponseEntity.ok(ApiResponse.<List<PaqueteDto>>builder()
                                .success(true)
                                .message("Paquetes activos obtenidos exitosamente")
                                .data(paquetes)
                                .build());
        }

        /**
         * Listar paquetes vigentes
         * ADMIN y CAJERO
         */
        @GetMapping("/vigentes")
        @PreAuthorize("hasAuthority('PAQUETE_LEER')")
        public ResponseEntity<ApiResponse<List<PaqueteDto>>> listarPaquetesVigentes() {

                List<PaqueteDto> paquetes = paqueteService.listarPaquetesVigentes();

                return ResponseEntity.ok(ApiResponse.<List<PaqueteDto>>builder()
                                .success(true)
                                .message("Paquetes vigentes obtenidos exitosamente")
                                .data(paquetes)
                                .build());
        }

        /**
         * Cambiar estado del paquete
         * Solo ADMIN
         */
        @PatchMapping("/{id}/estado")
        @PreAuthorize("hasAuthority('PAQUETE_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<Void>> cambiarEstado(
                        @PathVariable Long id,
                        @RequestParam String estado) {

                paqueteService.cambiarEstado(id, estado);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Estado del paquete actualizado exitosamente")
                                .build());
        }

        /**
         * Eliminar paquete
         * Solo ADMIN
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('PAQUETE_ELIMINAR')")
        public ResponseEntity<ApiResponse<Void>> eliminarPaquete(@PathVariable Long id) {

                paqueteService.eliminarPaquete(id);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Paquete eliminado exitosamente")
                                .build());
        }
}
