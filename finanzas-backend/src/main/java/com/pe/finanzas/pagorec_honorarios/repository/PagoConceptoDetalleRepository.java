package com.pe.finanzas.pagorec_honorarios.repository;

import com.pe.finanzas.pagorec_honorarios.entity.PagoConceptoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoConceptoDetalleRepository extends JpaRepository<PagoConceptoDetalle, Long> {
}
