package com.pe.finanzas.venta_registro.service;

import com.pe.finanzas.venta_registro.dto.LabOrdenDto;
import com.pe.finanzas.venta_registro.dto.PagarOrdenRequest;
import com.pe.finanzas.venta_registro.dto.VentaDetalleDto;
import com.pe.finanzas.venta_registro.dto.VentaRegistroDto;
import com.pe.finanzas.venta_registro.dto.VentaRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface VentaService {

    /**
     * Crear una nueva venta con sus detalles
     * Aplica cálculos con 4 decimales y guarda transaccionalmente
     * 
     * @param request Datos de la venta
     * @return Venta creada con todos los cálculos
     */
    VentaRegistroDto crearVenta(VentaRequest request);

    VentaRegistroDto obtenerVenta(Long idVenta);

    Page<VentaRegistroDto> listarVentas(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    Page<VentaRegistroDto> listarVentasPorSucursal(
            Long idSucursal,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Pageable pageable);

    List<VentaDetalleDto> obtenerDetalles(Long idVenta);

    void anularVenta(Long idVenta, String motivo);

    // --- MÉTODOS PARA ÓRDENES DE LABORATORIO ---
    LabOrdenDto obtenerOrden(Long idOrden);

    List<LabOrdenDto> buscarOrdenesPendientes(Long idPaciente);

    VentaRegistroDto pagarOrden(Long idOrden, PagarOrdenRequest request);
}

