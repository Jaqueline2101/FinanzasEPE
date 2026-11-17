package com.pe.finanzas.productos.service;

import com.pe.finanzas.productos.entity.Producto;
import com.pe.finanzas.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductoRepository productoRepository;

    /**
     * Sugiere qué lotes utilizar para cumplir con una cantidad solicitada siguiendo FEFO.
     */
    public List<LoteSugerido> sugerirDespachoFEFO(Long idCatalogo, BigDecimal cantidadSolicitada) {
        List<Producto> lotes = productoRepository.findAvailableLotesOrderByVencimiento(idCatalogo);
        List<LoteSugerido> sugerencia = new ArrayList<>();
        BigDecimal restante = cantidadSolicitada;

        for (Producto lote : lotes) {
            if (restante.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal stockDisponible = lote.getStockReal();
            BigDecimal aTomar = stockDisponible.min(restante);

            sugerencia.add(LoteSugerido.builder()
                    .idProducto(lote.getIdProducto())
                    .nroLote(lote.getRegistroInvimaLote())
                    .fechaVencimiento(lote.getFechaVencimiento())
                    .cantidadATomar(aTomar)
                    .stockRestanteEnLote(stockDisponible.subtract(aTomar))
                    .build());

            restante = restante.subtract(aTomar);
        }

        if (restante.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("Stock insuficiente para cumplir solicitud de {} unidades de idCatalogo {}. Faltan {}", 
                cantidadSolicitada, idCatalogo, restante);
        }

        return sugerencia;
    }

    /**
     * Retorna productos próximos a vencer o vencidos.
     */
    public List<Producto> obtenerAlertasVencimiento(Integer diasThreshold) {
        LocalDate limite = LocalDate.now().plusDays(diasThreshold != null ? diasThreshold : 90);
        return productoRepository.findAll().stream()
                .filter(p -> p.getStockReal().compareTo(BigDecimal.ZERO) > 0)
                .filter(p -> p.getFechaVencimiento() != null && p.getFechaVencimiento().isBefore(limite))
                .collect(Collectors.toList());
    }

    @lombok.Data
    @lombok.Builder
    public static class LoteSugerido {
        private Long idProducto;
        private String nroLote;
        private LocalDate fechaVencimiento;
        private BigDecimal cantidadATomar;
        private BigDecimal stockRestanteEnLote;
    }
}
