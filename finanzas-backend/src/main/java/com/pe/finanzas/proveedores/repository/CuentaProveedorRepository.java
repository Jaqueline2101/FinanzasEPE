package com.pe.finanzas.proveedores.repository;

import com.pe.finanzas.proveedores.entity.CuentaProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface CuentaProveedorRepository extends JpaRepository<CuentaProveedor, Long> {
    Optional<CuentaProveedor> findByProveedorId(Long idProveedor);

    @Query("SELECT SUM(cp.saldoTotal) FROM CuentaProveedor cp")
    java.math.BigDecimal totalCuentasPorPagar();

    @Query("SELECT cp.proveedor.razonSocial, cp.saldoTotal "
            +
            "FROM CuentaProveedor cp WHERE cp.saldoTotal > 0 ORDER BY cp.saldoTotal DESC")
    List<Object[]> deudasPorProveedorRaw();

    List<CuentaProveedor> findAllBySaldoTotalGreaterThan(java.math.BigDecimal saldo);
}
