package com.pe.finanzas.proveedores.controller;

import com.pe.finanzas.shared.dto.ApiResponse;
import com.pe.finanzas.proveedores.entity.CuentaProveedor;
import com.pe.finanzas.proveedores.entity.CuentaProveedorMovimiento;
import com.pe.finanzas.proveedores.service.CuentaProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/cuentas-proveedor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CuentaProveedorController {

        private final CuentaProveedorService cuentaService;

        @GetMapping("/proveedor/{id}")
        public ResponseEntity<ApiResponse<CuentaProveedor>> obtenerCuenta(@PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.<CuentaProveedor>builder()
                                .success(true)
                                .data(cuentaService.obtenerCuentaPorProveedor(id))
                                .build());
        }

        @GetMapping("/proveedor/{id}/movimientos")
        public ResponseEntity<ApiResponse<Page<CuentaProveedorMovimiento>>> listarMovimientos(
                        @PathVariable Long id, Pageable pageable) {
                return ResponseEntity.ok(ApiResponse.<Page<CuentaProveedorMovimiento>>builder()
                                .success(true)
                                .data(cuentaService.listarMovimientos(id, pageable))
                                .build());
        }

        @PostMapping("/proveedor/{id}/movimiento")
        public ResponseEntity<ApiResponse<CuentaProveedorMovimiento>> registrarMovimiento(
                        @PathVariable Long id,
                        @RequestBody Map<String, Object> payload) {

                String tipo = (String) payload.get("tipo");
                BigDecimal monto = new BigDecimal(payload.get("monto").toString());
                String descripcion = (String) payload.get("descripcion");

                String idUser = (String) payload.get("idUser");
                Long idSucursal = payload.get("idSucursal") != null ? Long.valueOf(payload.get("idSucursal").toString())
                                : null;
                boolean pagarDesdeCajaGeneral = payload.get("pagarDesdeCajaGeneral") != null
                                && (boolean) payload.get("pagarDesdeCajaGeneral");

                return ResponseEntity.ok(ApiResponse.<CuentaProveedorMovimiento>builder()
                                .success(true)
                                .message("Movimiento registrado correctamente")
                                .data(cuentaService.registrarMovimiento(id, tipo, monto, descripcion, idUser,
                                                idSucursal,
                                                pagarDesdeCajaGeneral))
                                .build());
        }

        @GetMapping("/pendientes")
        public ResponseEntity<ApiResponse<java.util.List<CuentaProveedor>>> listarPendientes() {
                return ResponseEntity.ok(ApiResponse.<java.util.List<CuentaProveedor>>builder()
                                .success(true)
                                .data(cuentaService.listarCuentasConSaldoPendiente())
                                .build());
        }
}
