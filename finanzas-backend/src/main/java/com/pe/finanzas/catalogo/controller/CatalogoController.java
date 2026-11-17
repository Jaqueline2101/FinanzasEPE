package com.pe.finanzas.catalogo.controller;

import com.pe.finanzas.catalogo.dto.request.CreateCatalogoRequest;
import com.pe.finanzas.catalogo.dto.request.UpdateCatalogoRequest;
import com.pe.finanzas.catalogo.dto.response.CatalogoResponse;
import com.pe.finanzas.catalogo.service.CatalogoService;
import com.pe.finanzas.shared.dto.ApiResponse;
import com.pe.finanzas.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CatalogoController {

        private final CatalogoService servicioService;

        @PostMapping
        public ResponseEntity<ApiResponse<CatalogoResponse>> createServicio(
                        @Valid @RequestBody CreateCatalogoRequest request) {

                CatalogoResponse response = servicioService.createServicio(request);

                ApiResponse<CatalogoResponse> apiResponse = ApiResponse.<CatalogoResponse>builder()
                                .success(true)
                                .message("Servicio creado exitosamente")
                                .data(response)
                                .build();

                return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<CatalogoResponse>> getServicioById(@PathVariable Long id) {
                CatalogoResponse response = servicioService.getServicioById(id);
                return ResponseEntity.ok(ApiResponse.<CatalogoResponse>builder()
                                .success(true)
                                .message("Servicio encontrado")
                                .data(response)
                                .build());
        }

        @GetMapping("/smart-search")
        public ResponseEntity<ApiResponse<com.pe.finanzas.catalogo.dto.response.SmartSearchResponse>> getSmartSearch(
                        @RequestParam String term,
                        @RequestParam(required = false) Long idConvenio,
                        @RequestParam(required = false) String tipoCliente) {

                com.pe.finanzas.catalogo.dto.response.SmartSearchResponse response = servicioService.searchSmart(term,
                                idConvenio, tipoCliente);

                return ResponseEntity
                                .ok(ApiResponse.<com.pe.finanzas.catalogo.dto.response.SmartSearchResponse>builder()
                                                .success(true)
                                                .message("Búsqueda inteligente realizada")
                                                .data(response)
                                                .build());
        }

        @GetMapping("/codigo/{codigo}")
        public ResponseEntity<ApiResponse<CatalogoResponse>> getServicioByCodigo(
                        @PathVariable String codigo) {

                CatalogoResponse response = servicioService.getServicioByCodigo(codigo);

                ApiResponse<CatalogoResponse> apiResponse = ApiResponse.<CatalogoResponse>builder()
                                .success(true)
                                .message("Servicio encontrado")
                                .data(response)
                                .build();

                return ResponseEntity.ok(apiResponse);
        }

        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<CatalogoResponse>>> getAllServicios(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();

                Pageable pageable = PageRequest.of(page, size, sort);
                PageResponse<CatalogoResponse> response = servicioService.getAllServicios(pageable);

                ApiResponse<PageResponse<CatalogoResponse>> apiResponse = ApiResponse
                                .<PageResponse<CatalogoResponse>>builder()
                                .success(true)
                                .message("Servicios obtenidos exitosamente")
                                .data(response)
                                .build();

                return ResponseEntity.ok(apiResponse);
        }

        @GetMapping("/list")
        public ResponseEntity<ApiResponse<List<CatalogoResponse>>> getAllServiciosList() {

                List<CatalogoResponse> response = servicioService.getAllServiciosList();

                ApiResponse<List<CatalogoResponse>> apiResponse = ApiResponse.<List<CatalogoResponse>>builder()
                                .success(true)
                                .message("Servicios obtenidos exitosamente")
                                .data(response)
                                .build();

                return ResponseEntity.ok(apiResponse);
        }

        @GetMapping("/estado/{estado}")
        public ResponseEntity<ApiResponse<PageResponse<CatalogoResponse>>> getServiciosByEstado(
                        @PathVariable Integer estado, // Corregido: de Boolean a Integer
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Pageable pageable = createPageable(page, size, sortBy, sortDir);
                PageResponse<CatalogoResponse> response = servicioService.getServiciosByEstado(estado, pageable);

                return ResponseEntity.ok(ApiResponse.<PageResponse<CatalogoResponse>>builder()
                                .success(true)
                                .message("Servicios filtrados por estado")
                                .data(response)
                                .build());
        }

        @GetMapping("/tipo-atencion/{idTipoAtencion}")
        public ResponseEntity<ApiResponse<PageResponse<CatalogoResponse>>> getServiciosByTipoAtencion(
                        @PathVariable Long idTipoAtencion,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Pageable pageable = createPageable(page, size, sortBy, sortDir);
                PageResponse<CatalogoResponse> response = servicioService.getServiciosByTipoAtencion(idTipoAtencion,
                                pageable);

                return ResponseEntity.ok(ApiResponse.<PageResponse<CatalogoResponse>>builder()
                                .success(true)
                                .message("Servicios filtrados por tipo de atención")
                                .data(response)
                                .build());
        }

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<PageResponse<CatalogoResponse>>> searchServicios(
                        @RequestParam String term,
                        @RequestParam(required = false) Long idConvenio,
                        @RequestParam(required = false) String tipoCliente,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();

                Pageable pageable = PageRequest.of(page, size, sort);
                PageResponse<CatalogoResponse> response = servicioService.searchServiciosConContexto(term, idConvenio,
                                tipoCliente, pageable);

                ApiResponse<PageResponse<CatalogoResponse>> apiResponse = ApiResponse
                                .<PageResponse<CatalogoResponse>>builder()
                                .success(true)
                                .message("Búsqueda realizada exitosamente")
                                .data(response)
                                .build();

                return ResponseEntity.ok(apiResponse);
        }

        @GetMapping("/search/estado/{estado}")
        public ResponseEntity<ApiResponse<PageResponse<CatalogoResponse>>> searchServiciosByEstado(
                        @RequestParam String term,
                        @PathVariable Integer estado, // Corregido: de Boolean a Integer
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Pageable pageable = createPageable(page, size, sortBy, sortDir);
                PageResponse<CatalogoResponse> response = servicioService.searchServiciosByEstado(term, estado,
                                pageable);

                return ResponseEntity.ok(ApiResponse.<PageResponse<CatalogoResponse>>builder()
                                .success(true)
                                .message("Búsqueda por estado realizada")
                                .data(response)
                                .build());
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<CatalogoResponse>> updateServicio(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateCatalogoRequest request) {

                CatalogoResponse response = servicioService.updateServicio(id, request);

                ApiResponse<CatalogoResponse> apiResponse = ApiResponse.<CatalogoResponse>builder()
                                .success(true)
                                .message("Servicio actualizado exitosamente")
                                .data(response)
                                .build();

                return ResponseEntity.ok(apiResponse);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteServicio(@PathVariable Long id) {

                servicioService.deleteServicio(id);

                ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                                .success(true)
                                .message("Servicio eliminado exitosamente")
                                .build();

                return ResponseEntity.ok(apiResponse);
        }

        @GetMapping("/descuentos/plan/{idPlan}")
        public ResponseEntity<ApiResponse<List<com.pe.finanzas.catalogo.dto.response.DescuentoConfiguradoDto>>> getDescuentosPorPlan(
                        @PathVariable Long idPlan) {
                List<com.pe.finanzas.catalogo.dto.response.DescuentoConfiguradoDto> response = servicioService
                                .getDescuentosPorPlan(idPlan);
                return ResponseEntity.ok(
                                ApiResponse.<List<com.pe.finanzas.catalogo.dto.response.DescuentoConfiguradoDto>>builder()
                                                .success(true)
                                                .message("Descuentos obtenidos exitosamente")
                                                .data(response)
                                                .build());
        }

        @PostMapping("/aplicar-descuento")
        public ResponseEntity<ApiResponse<Void>> aplicarDescuento(
                        @Valid @RequestBody com.pe.finanzas.catalogo.dto.request.AplicarDescuentoDto request) {

                servicioService.aplicarDescuento(request);

                ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                                .success(true)
                                .message("Descuento aplicado exitosamente")
                                .build();

                return ResponseEntity.ok(apiResponse);
        }

        private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();
                return PageRequest.of(page, size, sort);
        }
}
