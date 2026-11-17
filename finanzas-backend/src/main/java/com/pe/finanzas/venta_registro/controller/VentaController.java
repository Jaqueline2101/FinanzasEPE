package com.pe.finanzas.venta_registro.controller;

import com.pe.finanzas.shared.dto.ApiResponse;
import com.pe.finanzas.venta_registro.dto.VentaDetalleDto;
import com.pe.finanzas.venta_registro.dto.VentaRegistroDto;
import com.pe.finanzas.venta_registro.dto.VentaRequest;
import com.pe.finanzas.venta_registro.dto.LabOrdenDto;
import com.pe.finanzas.venta_registro.dto.PagarOrdenRequest;
import com.pe.finanzas.venta_registro.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VentaController {

        private final VentaService ventaService;

        /**
         * Crear una nueva venta
         * POST /api/ventas
         * 
         * @param request Datos de la venta (cabecera + items)
         * @return Venta creada con todos los cálculos
         */
        @PostMapping
        @PreAuthorize("hasAuthority('VENTA_CREAR')")
        public ResponseEntity<ApiResponse<VentaRegistroDto>> crearVenta(
                        @Valid @RequestBody VentaRequest request) {

                VentaRegistroDto venta = ventaService.crearVenta(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<VentaRegistroDto>builder()
                                                .success(true)
                                                .message("Venta creada exitosamente")
                                                .data(venta)
                                                .build());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('VENTA_LEER')")
        public ResponseEntity<ApiResponse<VentaRegistroDto>> obtenerVenta(@PathVariable Long id) {

                VentaRegistroDto venta = ventaService.obtenerVenta(id);

                return ResponseEntity.ok(ApiResponse.<VentaRegistroDto>builder()
                                .success(true)
                                .message("Venta obtenida exitosamente")
                                .data(venta)
                                .build());
        }

        @GetMapping
        @PreAuthorize("hasAuthority('VENTA_LEER')")
        public ResponseEntity<ApiResponse<Page<VentaRegistroDto>>> listarVentas(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                        @PageableDefault(page = 0, size = 20, sort = "fecha") Pageable pageable) {

                Page<VentaRegistroDto> ventas = ventaService.listarVentas(fechaInicio, fechaFin, pageable);

                return ResponseEntity.ok(ApiResponse.<Page<VentaRegistroDto>>builder()
                                .success(true)
                                .message("Ventas obtenidas exitosamente")
                                .data(ventas)
                                .build());
        }

        @GetMapping("/sucursal/{idSucursal}")
        @PreAuthorize("hasAuthority('VENTA_LEER')")
        public ResponseEntity<ApiResponse<Page<VentaRegistroDto>>> listarVentasPorSucursal(
                        @PathVariable Long idSucursal,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                        @PageableDefault(page = 0, size = 20, sort = "fecha") Pageable pageable) {

                Page<VentaRegistroDto> ventas = ventaService.listarVentasPorSucursal(
                                idSucursal, fechaInicio, fechaFin, pageable);

                return ResponseEntity.ok(ApiResponse.<Page<VentaRegistroDto>>builder()
                                .success(true)
                                .message("Ventas de la sucursal obtenidas exitosamente")
                                .data(ventas)
                                .build());
        }

        @GetMapping("/{id}/detalles")
        @PreAuthorize("hasAuthority('VENTA_LEER')")
        public ResponseEntity<ApiResponse<List<VentaDetalleDto>>> obtenerDetalles(@PathVariable Long id) {

                List<VentaDetalleDto> detalles = ventaService.obtenerDetalles(id);

                return ResponseEntity.ok(ApiResponse.<List<VentaDetalleDto>>builder()
                                .success(true)
                                .message("Detalles obtenidos exitosamente")
                                .data(detalles)
                                .build());
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('VENTA_ELIMINAR')")
        public ResponseEntity<ApiResponse<Void>> anularVenta(
                        @PathVariable Long id,
                        @RequestParam String motivo) {

                ventaService.anularVenta(id, motivo);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Venta anulada exitosamente")
                                .build());
        }

        // --- ENDPOINTS PARA ÓRDENES DE LABORATORIO ---

        @GetMapping("/ordenes/{idOrden}")
        @PreAuthorize("hasAuthority('VENTA_LEER')")
        public ResponseEntity<ApiResponse<LabOrdenDto>> obtenerOrden(@PathVariable Long idOrden) {
                LabOrdenDto orden = ventaService.obtenerOrden(idOrden);
                return ResponseEntity.ok(ApiResponse.<LabOrdenDto>builder()
                                .success(true)
                                .message("Orden obtenida exitosamente")
                                .data(orden)
                                .build());
        }

        @GetMapping("/ordenes/pendientes")

        @PreAuthorize("hasAuthority('VENTA_LEER')")
        public ResponseEntity<ApiResponse<List<LabOrdenDto>>> buscarOrdenesPendientes() {
                List<LabOrdenDto> ordenes = ventaService.buscarOrdenesPendientes(null);
                return ResponseEntity.ok(ApiResponse.<List<LabOrdenDto>>builder()
                                .success(true)
                                .message("Órdenes pendientes obtenidas exitosamente")
                                .data(ordenes)
                                .build());
        }

        @GetMapping("/ordenes/pendientes/{idPaciente}")
        @PreAuthorize("hasAuthority('VENTA_LEER')")
        public ResponseEntity<ApiResponse<List<LabOrdenDto>>> buscarOrdenesPendientesPorPaciente(
                        @PathVariable Long idPaciente) {
                List<LabOrdenDto> ordenes = ventaService.buscarOrdenesPendientes(idPaciente);
                return ResponseEntity.ok(ApiResponse.<List<LabOrdenDto>>builder()
                                .success(true)
                                .message("Órdenes pendientes del paciente obtenidas exitosamente")
                                .data(ordenes)
                                .build());
        }

        @PostMapping("/pagar-orden/{idOrden}")
        @PreAuthorize("hasAuthority('VENTA_CREAR')")
        public ResponseEntity<ApiResponse<VentaRegistroDto>> pagarOrden(
                        @PathVariable Long idOrden,
                        @RequestBody PagarOrdenRequest request) {
                VentaRegistroDto venta = ventaService.pagarOrden(idOrden, request);
                return ResponseEntity.ok(ApiResponse.<VentaRegistroDto>builder()
                                .success(true)
                                .message("Orden pagada y venta registrada exitosamente")
                                .data(venta)
                                .build());
        }

}
