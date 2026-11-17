package com.pe.finanzas.pago_personal.repository;

import com.pe.finanzas.pagorec_honorarios.entity.PagoComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoPersonalRepository extends JpaRepository<PagoComprobante, Long> {
    // Queries general (CRUD) for Personal Payments
    List<PagoComprobante> findByNumeroDocumento(String numeroDocumento);
}
