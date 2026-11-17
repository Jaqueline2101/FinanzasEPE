package com.pe.finanzas.proveedores.service.impl;

import com.pe.finanzas.proveedores.entity.CuentaProveedor;
import com.pe.finanzas.proveedores.entity.CuentaProveedorMovimiento;
import com.pe.finanzas.proveedores.entity.Proveedor;
import com.pe.finanzas.proveedores.repository.CuentaProveedorMovimientoRepository;
import com.pe.finanzas.proveedores.repository.CuentaProveedorRepository;
import com.pe.finanzas.proveedores.repository.ProveedorRepository;
import com.pe.finanzas.proveedores.service.CuentaProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CuentaProveedorServiceImpl implements CuentaProveedorService {

    private final CuentaProveedorRepository cuentaRepository;
    private final CuentaProveedorMovimientoRepository movimientoRepository;
    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional
    public CuentaProveedor obtenerCuentaPorProveedor(Long idProveedor) {
        return cuentaRepository.findByProveedorId(idProveedor)
                .orElseGet(() -> {
                    Proveedor proveedor = proveedorRepository.findById(idProveedor)
                            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
                    CuentaProveedor nuevaCuenta = CuentaProveedor.builder()
                            .proveedor(proveedor)
                            .saldoTotal(BigDecimal.ZERO)
                            .build();
                    return cuentaRepository.save(nuevaCuenta);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaProveedorMovimiento> listarMovimientos(Long idProveedor, Pageable pageable) {
        CuentaProveedor cuenta = obtenerCuentaPorProveedor(idProveedor);
        return movimientoRepository.findByCuentaIdOrderByFechaRegistroDesc(cuenta.getId(), pageable);
    }

    @Override
    @Transactional
    public CuentaProveedorMovimiento registrarMovimiento(Long idProveedor, String tipo, BigDecimal monto,
            String descripcion, String idUser, Long idSucursal, boolean pagarDesdeCajaGeneral) {
        CuentaProveedor cuenta = obtenerCuentaPorProveedor(idProveedor);

        CuentaProveedorMovimiento movimiento = CuentaProveedorMovimiento.builder()
                .cuenta(cuenta)
                .tipo(tipo)
                .monto(monto)
                .descripcion(descripcion)
                .idUser(idUser)
                .build();

        // Actualizar saldo de cuenta proveedor
        if ("ABONO".equalsIgnoreCase(tipo)) {
            cuenta.setSaldoTotal(cuenta.getSaldoTotal().subtract(monto));
        } else if ("CARGO".equalsIgnoreCase(tipo)) {
            cuenta.setSaldoTotal(cuenta.getSaldoTotal().add(monto));
        }

        cuentaRepository.save(cuenta);
        return movimientoRepository.save(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<CuentaProveedor> listarCuentasConSaldoPendiente() {
        return cuentaRepository.findAllBySaldoTotalGreaterThan(BigDecimal.ZERO);
    }
}
