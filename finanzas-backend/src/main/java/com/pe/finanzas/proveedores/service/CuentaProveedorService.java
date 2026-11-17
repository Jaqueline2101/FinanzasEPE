package com.pe.finanzas.proveedores.service;

import com.pe.finanzas.proveedores.entity.CuentaProveedor;
import com.pe.finanzas.proveedores.entity.CuentaProveedorMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface CuentaProveedorService {
    CuentaProveedor obtenerCuentaPorProveedor(Long idProveedor);

    Page<CuentaProveedorMovimiento> listarMovimientos(Long idProveedor, Pageable pageable);

    CuentaProveedorMovimiento registrarMovimiento(Long idProveedor, String tipo, BigDecimal monto, String descripcion,
            String idUser, Long idSucursal, boolean pagarDesdeCajaGeneral);

    java.util.List<CuentaProveedor> listarCuentasConSaldoPendiente();
}
