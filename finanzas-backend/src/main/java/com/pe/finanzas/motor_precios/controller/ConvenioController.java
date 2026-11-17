package com.pe.finanzas.motor_precios.controller;

import com.pe.finanzas.motor_precios.dto.BulkVinculationRequest;
import com.pe.finanzas.motor_precios.dto.ConvenioDto;
import com.pe.finanzas.motor_precios.dto.ConvenioRequest;
import com.pe.finanzas.motor_precios.service.ConvenioService;
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

@RestController
@RequestMapping("/api/convenios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConvenioController {

        private final ConvenioService convenioService;

        @PostMapping
        @PreAuthorize("hasAuthority('CONVENIO_CREAR')")
        public ResponseEntity<ApiResponse<ConvenioDto>> crearConvenio(
                        @Valid @RequestBody ConvenioRequest request) {

                try {
                        ConvenioDto convenio = convenioService.crearConvenio(request);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<ConvenioDto>builder()
                                                        .success(true)
                                                        .message("Convenio creado exitosamente")
                                                        .data(convenio)
                                                        .build());
                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<ConvenioDto>builder()
                                                        .success(false)
                                                        .message("Error al crear convenio: " + e.getMessage())
                                                        .build());
                }
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('CONVENIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<ConvenioDto>> actualizarConvenio(
                        @PathVariable Long id,
                        @Valid @RequestBody ConvenioRequest request) {

                try {
                        ConvenioDto convenio = convenioService.actualizarConvenio(id, request);

                        return ResponseEntity.ok(ApiResponse.<ConvenioDto>builder()
                                        .success(true)
                                        .message("Convenio actualizado exitosamente")
                                        .data(convenio)
                                        .build());
                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<ConvenioDto>builder()
                                                        .success(false)
                                                        .message("Error al actualizar convenio: " + e.getMessage())
                                                        .build());
                }
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('CONVENIO_LEER')")
        public ResponseEntity<ApiResponse<ConvenioDto>> obtenerConvenio(@PathVariable Long id) {

                ConvenioDto convenio = convenioService.obtenerConvenio(id);

                return ResponseEntity.ok(ApiResponse.<ConvenioDto>builder()
                                .success(true)
                                .message("Convenio obtenido exitosamente")
                                .data(convenio)
                                .build());
        }

        @GetMapping("/codigo/{codigo}")
        @PreAuthorize("hasAuthority('CONVENIO_LEER')")
        public ResponseEntity<ApiResponse<ConvenioDto>> obtenerPorCodigo(@PathVariable String codigo) {
                ConvenioDto convenio = convenioService.obtenerPorCodigo(codigo);
                return ResponseEntity.ok(ApiResponse.<ConvenioDto>builder()
                        .success(true)
                        .message("Convenio obtenido por código exitosamente")
                        .data(convenio)
                        .build());
        }

        @GetMapping
        @PreAuthorize("hasAuthority('CONVENIO_LEER')")
        public ResponseEntity<ApiResponse<Page<ConvenioDto>>> listarConvenios(
                        @PageableDefault(page = 0, size = 20, sort = "idPlan", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

                Page<ConvenioDto> convenios = convenioService.listarConvenios(pageable);

                return ResponseEntity.ok(ApiResponse.<Page<ConvenioDto>>builder()
                                .success(true)
                                .message("Convenios obtenidos exitosamente")
                                .data(convenios)
                                .build());
        }

        @GetMapping("/por-compania/{id}")
        @PreAuthorize("hasAuthority('CONVENIO_LEER')")
        public ResponseEntity<ApiResponse<List<ConvenioDto>>> listarPlanesPorCompania(@PathVariable Long id) {

                List<ConvenioDto> planes = convenioService.listarPlanesPorCompania(id);

                return ResponseEntity.ok(ApiResponse.<List<ConvenioDto>>builder()
                                .success(true)
                                .message("Planes de la compañía obtenidos exitosamente")
                                .data(planes)
                                .build());
        }

        @GetMapping("/activos")
        @PreAuthorize("hasAuthority('CONVENIO_LEER')")
        public ResponseEntity<ApiResponse<List<ConvenioDto>>> listarConveniosActivos() {

                List<ConvenioDto> convenios = convenioService.listarConveniosActivos();

                return ResponseEntity.ok(ApiResponse.<List<ConvenioDto>>builder()
                                .success(true)
                                .message("Convenios activos obtenidos exitosamente")
                                .data(convenios)
                                .build());
        }

        @GetMapping("/vigentes")
        @PreAuthorize("hasAuthority('CONVENIO_LEER')")
        public ResponseEntity<ApiResponse<List<ConvenioDto>>> listarConveniosVigentes() {

                List<ConvenioDto> convenios = convenioService.listarConveniosVigentes();

                return ResponseEntity.ok(ApiResponse.<List<ConvenioDto>>builder()
                                .success(true)
                                .message("Convenios vigentes obtenidos exitosamente")
                                .data(convenios)
                                .build());
        }

        @PatchMapping("/{id}/estado")
        @PreAuthorize("hasAuthority('CONVENIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<Void>> cambiarEstado(
                        @PathVariable Long id,
                        @RequestParam String estado) {

                convenioService.cambiarEstado(id, estado);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Estado del convenio actualizado exitosamente")
                                .build());
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('CONVENIO_ELIMINAR')")
        public ResponseEntity<ApiResponse<Void>> eliminarConvenio(@PathVariable Long id) {

                convenioService.eliminarConvenio(id);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Convenio eliminado exitosamente")
                                .build());
        }


        @PostMapping("/{id}/reglas")
        @PreAuthorize("hasAuthority('CONVENIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<com.pe.finanzas.motor_precios.dto.DescuentoConvenioDto>> agregarRegla(
                        @PathVariable Long id,
                        @Valid @RequestBody com.pe.finanzas.motor_precios.dto.ConvenioRequest.DescuentoRequest request) {

                com.pe.finanzas.motor_precios.dto.DescuentoConvenioDto regla = convenioService.agregarRegla(id,
                                request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<com.pe.finanzas.motor_precios.dto.DescuentoConvenioDto>builder()
                                                .success(true)
                                                .message("Regla de descuento agregada exitosamente")
                                                .data(regla)
                                                .build());
        }

        @DeleteMapping("/{id}/reglas/{idRegla}")
        @PreAuthorize("hasAuthority('CONVENIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<Void>> eliminarRegla(
                        @PathVariable Long id,
                        @PathVariable Long idRegla) {

                convenioService.eliminarRegla(id, idRegla);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Regla de descuento eliminada exitosamente")
                                .build());
        }

        @PutMapping("/{id}/reglas/{idRegla}")
        @PreAuthorize("hasAuthority('CONVENIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<com.pe.finanzas.motor_precios.dto.DescuentoConvenioDto>> actualizarRegla(
                        @PathVariable Long id,
                        @PathVariable Long idRegla,
                        @Valid @RequestBody com.pe.finanzas.motor_precios.dto.ConvenioRequest.DescuentoRequest request) {

                com.pe.finanzas.motor_precios.dto.DescuentoConvenioDto regla = convenioService.actualizarRegla(id,
                                idRegla, request);

                return ResponseEntity.ok(ApiResponse.<com.pe.finanzas.motor_precios.dto.DescuentoConvenioDto>builder()
                                .success(true)
                                .message("Regla de descuento actualizada exitosamente")
                                .data(regla)
                                .build());
        }

        @PostMapping("/vincular-empresa")
        @PreAuthorize("hasAuthority('CONVENIO_CREAR')")
        public ResponseEntity<ApiResponse<ConvenioDto>> vincularEmpresa(
                        @RequestParam Long idCompania,
                        @RequestParam Long idEmpresa) {

                ConvenioDto convenio = convenioService.crearPlanParaEmpresa(idCompania, idEmpresa);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ConvenioDto>builder()
                                                .success(true)
                                                .message("Empresa vinculada exitosamente")
                                                .data(convenio)
                                                .build());
        }

        @PostMapping("/vincular-empresas-masivo")
        @PreAuthorize("hasAuthority('CONVENIO_CREAR')")
        public ResponseEntity<ApiResponse<List<ConvenioDto>>> vincularEmpresasMasivo(
                        @RequestBody BulkVinculationRequest request) {

                List<ConvenioDto> convenios = convenioService.vincularEmpresasMasivo(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<List<ConvenioDto>>builder()
                                                .success(true)
                                                .message("Empresas vinculadas exitosamente")
                                                .data(convenios)
                                                .build());
        }

        @GetMapping("/generar-codigo/{idCompania}")
        @PreAuthorize("hasAuthority('CONVENIO_CREAR')")
        public ResponseEntity<ApiResponse<String>> generarCodigo(@PathVariable Long idCompania) {
                String codigo = convenioService.generarCodigo(idCompania);

                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .success(true)
                                .message("Código generado correctamente")
                                .data(codigo)
                                .build());
        }

        @PostMapping("/{idDestino}/clonar-desde/{idOrigen}")
        @PreAuthorize("hasAuthority('CONVENIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<Void>> clonarDescuentos(
                        @PathVariable Long idOrigen,
                        @PathVariable Long idDestino) {

                try {
                        convenioService.clonarDescuentos(idOrigen, idDestino);

                        return ResponseEntity.ok(ApiResponse.<Void>builder()
                                        .success(true)
                                        .message("Descuentos clonados exitosamente")
                                        .build());
                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Error al clonar descuentos: " + e.getMessage())
                                                        .build());
                }
        }
}
