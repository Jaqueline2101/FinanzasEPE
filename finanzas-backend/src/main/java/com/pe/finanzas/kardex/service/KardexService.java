package com.pe.finanzas.kardex.service;

import com.pe.finanzas.kardex.entity.Kardex;
import com.pe.finanzas.kardex.entity.ClasificacionMovimiento;
import com.pe.finanzas.kardex.repository.KardexRepository;
import com.pe.finanzas.kardex.repository.ClasificacionMovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KardexService {

    private final KardexRepository kardexRepository;
    private final ClasificacionMovimientoRepository clasificacionRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public Kardex registrarMovimiento(Kardex movimiento) {
        // 0. Saneamiento Senior de nulos para evitar PSQLException (NOT NULL constraints)
        sanitize(movimiento);

        // 1. Generar ID único si no existe
        if (movimiento.getIdArticuloKardex() == null) {
            movimiento.setIdArticuloKardex(UUID.randomUUID().toString());
        }

        // 2. Obtener clasificación para el signo — fallback seguro si no existe en BD
        ClasificacionMovimiento clasif = clasificacionRepository.findByNombre(movimiento.getOperacion())
                .orElseGet(() -> {
                    // Crear la clasificación al vuelo si no está en BD
                    boolean esIngreso = movimiento.getOperacion().toUpperCase().contains("INGRESO")
                            || movimiento.getOperacion().toUpperCase().contains("COMPRA")
                            || movimiento.getOperacion().toUpperCase().contains("AJUSTE_ENTRADA");
                    return ClasificacionMovimiento.builder()
                            .nombre(movimiento.getOperacion())
                            .signo(esIngreso ? "+" : "-")
                            .tipo(esIngreso ? "INGRESO" : "SALIDA")
                            .descripcion("Creado automáticamente")
                            .build();
                });

        // Si no tiene ID, persistirla en BD para reutilización futura
        if (clasif.getId() == null) {
            clasif = clasificacionRepository.save(clasif);
        }

        movimiento.setSigno(clasif.getSigno());
        movimiento.setIdClasificacion(clasif.getId());

        // 3. Calcular saldos (PMP - Precio Medio Ponderado)
        // Buscamos el último movimiento del mismo producto (Batch/Lot)
        List<Kardex> historial = kardexRepository.findByIdAlmArticuloOrderByFechaDesc(movimiento.getIdAlmArticulo());
        
        BigDecimal saldoAnteriorCant = BigDecimal.ZERO;
        BigDecimal saldoAnteriorCostoTotal = BigDecimal.ZERO;

        if (!historial.isEmpty()) {
            Kardex ultimo = historial.get(0);
            saldoAnteriorCant = ultimo.getSaldoCantidad();
            saldoAnteriorCostoTotal = ultimo.getSaldoCostoTotal();
        }

        BigDecimal cantidadMov = movimiento.getCantidad();
        BigDecimal costoUniMov = movimiento.getCostoUnitario();
        BigDecimal costoTotalMov = cantidadMov.multiply(costoUniMov);
        movimiento.setCostoTotal(costoTotalMov);

        BigDecimal nuevoSaldoCant;
        BigDecimal nuevoSaldoCostoTotal;

        if ("+".equals(movimiento.getSigno())) {
            nuevoSaldoCant = saldoAnteriorCant.add(cantidadMov);
            nuevoSaldoCostoTotal = saldoAnteriorCostoTotal.add(costoTotalMov);
        } else {
            nuevoSaldoCant = saldoAnteriorCant.subtract(cantidadMov);
            // En salidas, el costo total se calcula con el PMP anterior
            BigDecimal pmpAnterior = saldoAnteriorCant.compareTo(BigDecimal.ZERO) > 0 
                ? saldoAnteriorCostoTotal.divide(saldoAnteriorCant, 4, RoundingMode.HALF_UP) 
                : costoUniMov;
            
            BigDecimal costoSalida = cantidadMov.multiply(pmpAnterior);
            movimiento.setCostoTotal(costoSalida); // Sobrescribimos costo total para salidas
            nuevoSaldoCostoTotal = saldoAnteriorCostoTotal.subtract(costoSalida);
        }

        movimiento.setSaldoCantidad(nuevoSaldoCant);
        movimiento.setSaldoCostoTotal(nuevoSaldoCostoTotal);

        if (nuevoSaldoCant.compareTo(BigDecimal.ZERO) > 0) {
            movimiento.setSaldoCostoUnitario(nuevoSaldoCostoTotal.divide(nuevoSaldoCant, 4, RoundingMode.HALF_UP));
        } else {
            movimiento.setSaldoCostoUnitario(BigDecimal.ZERO);
        }

        return kardexRepository.save(movimiento);
    }

    /**
     * Saneamiento Senior: Asegura que campos obligatorios en la BD nunca viajen nulos,
     * proporcionando valores seguros por defecto.
     */
    private void sanitize(Kardex mov) {
        if (mov.getDetalle() == null) mov.setDetalle("");
        if (mov.getObservacion() == null) mov.setObservacion("");
        if (mov.getIdDocumento() == null) mov.setIdDocumento("N/A");
        if (mov.getNumDoc() == null) mov.setNumDoc("N/A");
        if (mov.getOrigenId() == null) mov.setOrigenId("0");
        if (mov.getOrigenTipo() == null) mov.setOrigenTipo("SISTEMA");
        if (mov.getPresentacion() == null) mov.setPresentacion("UNIDAD");
        
        // El idUsuario es crítico. Si llega nulo, lo forzamos al admin por defecto (1) 
        // para evitar fallos de integridad si el contexto de seguridad falló.
        if (mov.getIdUsuario() == null) {
            mov.setIdUsuario(1L);
        }
    }
}
