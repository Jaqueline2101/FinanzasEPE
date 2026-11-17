package com.pe.finanzas.caja_general.service.impl;

import com.pe.finanzas.atributos.entity.MetodoPago;
import com.pe.finanzas.atributos.repository.MetodoPagoRepository;
import com.pe.finanzas.caja_general.dto.CajaGeneralMovimientoRequest;
import com.pe.finanzas.caja_general.dto.CajaGeneralMovimientoResponse;
import com.pe.finanzas.caja_general.dto.CajaGeneralResponse;
import com.pe.finanzas.caja_general.dto.CajaGeneralSaldoResponse;
import com.pe.finanzas.caja_general.entity.CajaGeneral;
import com.pe.finanzas.caja_general.entity.CajaGeneralMovimiento;
import com.pe.finanzas.caja_general.entity.CajaGeneralSaldo;
import com.pe.finanzas.caja_general.repository.CajaGeneralMovimientoRepository;
import com.pe.finanzas.caja_general.repository.CajaGeneralRepository;
import com.pe.finanzas.caja_general.repository.CajaGeneralSaldoRepository;
import com.pe.finanzas.caja_general.service.CajaGeneralService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CajaGeneralServiceImpl implements CajaGeneralService {

    private final CajaGeneralRepository cajaGeneralRepository;
    private final CajaGeneralMovimientoRepository movimientoRepository;
    private final CajaGeneralSaldoRepository saldoRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    @Override
    @Transactional
    public CajaGeneralResponse obtenerPorSucursal(Long idSucursal) {
        CajaGeneral caja = obtenerOInicializar(idSucursal);
        List<CajaGeneralSaldo> saldos = saldoRepository.findByCajaGeneralId(caja.getId());

        return mapToResponse(caja, saldos);
    }

    @Override
    @Transactional
    public CajaGeneralMovimientoResponse registrarMovimiento(CajaGeneralMovimientoRequest request) {
        CajaGeneral caja = obtenerOInicializar(request.getIdSucursal());

        if (request.getTipo() == CajaGeneralMovimiento.TipoMovimiento.TRANSFERENCIA_ENTRE_METODOS) {
            return procesarTransferencia(caja, request);
        }

        MetodoPago metodoPago = buscarMetodoPago(request.getMetodoPago());

        // Validar saldo si es un egreso
        if (request.getTipo() == CajaGeneralMovimiento.TipoMovimiento.EGRESO) {
            validarSaldoSuficiente(caja, metodoPago, request.getMonto());
        }

        CajaGeneralMovimiento movimiento = CajaGeneralMovimiento.builder()
                .cajaGeneral(caja)
                .tipo(request.getTipo())
                .monto(request.getMonto())
                .descripcion(request.getDescripcion())
                .referencia(request.getReferencia())
                .metodoPago(metodoPago)
                .fecha(LocalDateTime.now())
                .usuario(request.getUsuario())
                .build();

        actualizarSaldos(caja, metodoPago, request.getMonto(), request.getTipo(), true);

        return mapToMovimientoResponse(movimientoRepository.save(movimiento));
    }

    private CajaGeneralMovimientoResponse procesarTransferencia(CajaGeneral caja,
            CajaGeneralMovimientoRequest request) {
        MetodoPago origen = buscarMetodoPago(request.getMetodoPagoOrigen());
        MetodoPago destino = buscarMetodoPago(request.getMetodoPagoDestino());

        // Validar saldo suficiente en origen
        validarSaldoSuficiente(caja, origen, request.getMonto());

        // Descontar de origen
        actualizarSaldos(caja, origen, request.getMonto(), CajaGeneralMovimiento.TipoMovimiento.EGRESO, true);
        // Aumentar en destino
        actualizarSaldos(caja, destino, request.getMonto(), CajaGeneralMovimiento.TipoMovimiento.INGRESO, true);

        CajaGeneralMovimiento movimiento = CajaGeneralMovimiento.builder()
                .cajaGeneral(caja)
                .tipo(request.getTipo())
                .monto(request.getMonto())
                .descripcion(String.format("Transferencia de %s a %s: %s",
                        origen.getDescripcion(), destino.getDescripcion(), request.getDescripcion()))
                .referencia(request.getReferencia())
                .metodoPago(origen) // Se registra bajo el origen como referencia
                .metodoPagoOrigen(origen.getDescripcion())
                .metodoPagoDestino(destino.getDescripcion())
                .fecha(LocalDateTime.now())
                .usuario(request.getUsuario())
                .build();

        return mapToMovimientoResponse(movimientoRepository.save(movimiento));
    }

    @Override
    @Transactional
    public CajaGeneralMovimientoResponse actualizarMovimiento(Long id, CajaGeneralMovimientoRequest request) {
        CajaGeneralMovimiento mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado: " + id));

        CajaGeneral caja = mov.getCajaGeneral();

        // 1. Revertir impacto anterior
        actualizarSaldos(caja, mov.getMetodoPago(), mov.getMonto(), mov.getTipo(), false);

        // 2. Aplicar nuevo impacto
        MetodoPago nuevoMetodo = buscarMetodoPago(request.getMetodoPago());
        actualizarSaldos(caja, nuevoMetodo, request.getMonto(), request.getTipo(), true);

        mov.setTipo(request.getTipo());
        mov.setMonto(request.getMonto());
        mov.setDescripcion(request.getDescripcion());
        mov.setReferencia(request.getReferencia());
        mov.setMetodoPago(nuevoMetodo);
        mov.setUsuario(request.getUsuario());

        return mapToMovimientoResponse(movimientoRepository.save(mov));
    }

    @Override
    @Transactional
    public void eliminarMovimiento(Long id) {
        CajaGeneralMovimiento mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado: " + id));

        actualizarSaldos(mov.getCajaGeneral(), mov.getMetodoPago(), mov.getMonto(), mov.getTipo(), false);
        movimientoRepository.delete(mov);
    }

    private void actualizarSaldos(CajaGeneral caja, MetodoPago mp, BigDecimal monto,
            CajaGeneralMovimiento.TipoMovimiento tipo, boolean esNuevo) {
        boolean sumar = esIngreso(tipo);
        if (!esNuevo)
            sumar = !sumar; // Si revertimos, invertimos la operación

        // Actualizar Saldo General
        if (sumar) {
            caja.setSaldoActual(caja.getSaldoActual().add(monto));
        } else {
            caja.setSaldoActual(caja.getSaldoActual().subtract(monto));
        }
        cajaGeneralRepository.save(caja);

        // Actualizar Saldo Específico por Método
        CajaGeneralSaldo saldoMetodo = saldoRepository
                .findByCajaGeneralIdAndMetodoPagoId(caja.getId(), mp.getId())
                .orElseGet(() -> saldoRepository.save(CajaGeneralSaldo.builder()
                        .cajaGeneral(caja)
                        .metodoPago(mp)
                        .saldoActual(BigDecimal.ZERO)
                        .build()));

        if (sumar) {
            saldoMetodo.setSaldoActual(saldoMetodo.getSaldoActual().add(monto));
        } else {
            saldoMetodo.setSaldoActual(saldoMetodo.getSaldoActual().subtract(monto));
        }
        saldoRepository.save(saldoMetodo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CajaGeneralMovimientoResponse> listarMovimientos(Long idSucursal, String metodoPago,
            Pageable pageable) {
        CajaGeneral caja = obtenerOInicializar(idSucursal);

        if (metodoPago != null && !metodoPago.isEmpty() && !"TODOS".equalsIgnoreCase(metodoPago)) {
            return movimientoRepository
                    .findByCajaGeneralIdAndMetodoPagoDescripcionOrderByFechaDesc(caja.getId(), metodoPago, pageable)
                    .map(this::mapToMovimientoResponse);
        }

        return movimientoRepository.findByCajaGeneralIdOrderByFechaDesc(caja.getId(), pageable)
                .map(this::mapToMovimientoResponse);
    }

    private void validarSaldoSuficiente(CajaGeneral caja, MetodoPago mp, BigDecimal monto) {
        CajaGeneralSaldo saldoMetodo = saldoRepository
                .findByCajaGeneralIdAndMetodoPagoId(caja.getId(), mp.getId())
                .orElse(null);

        BigDecimal saldoActual = (saldoMetodo != null) ? saldoMetodo.getSaldoActual() : BigDecimal.ZERO;

        if (saldoActual.compareTo(monto) < 0) {
            throw new RuntimeException(
                    "Saldo insuficiente en " + mp.getDescripcion() + ". Saldo disponible: S/ " + saldoActual);
        }
    }

    private MetodoPago buscarMetodoPago(String desc) {
        String descripcion = (desc != null && !desc.trim().isEmpty()) ? desc.toUpperCase() : "EFECTIVO";
        return metodoPagoRepository.findByDescripcionIgnoreCase(descripcion)
                .orElseGet(() -> {
                    MetodoPago nuevo = MetodoPago.builder()
                            .descripcion(descripcion)
                            .build();
                    return metodoPagoRepository.save(nuevo);
                });
    }

    private CajaGeneral obtenerOInicializar(Long idSucursal) {
        return cajaGeneralRepository.findByIdSucursal(idSucursal)
                .orElseGet(() -> {
                    CajaGeneral nuevaCaja = CajaGeneral.builder()
                            .nombre("CAJA GENERAL SUCURSAL " + idSucursal)
                            .idSucursal(idSucursal)
                            .saldoActual(BigDecimal.ZERO)
                            .usuarioCreacion("SYSTEM")
                            .build();
                    return cajaGeneralRepository.save(nuevaCaja);
                });
    }

    private boolean esIngreso(CajaGeneralMovimiento.TipoMovimiento tipo) {
        return tipo == CajaGeneralMovimiento.TipoMovimiento.INGRESO ||
                tipo == CajaGeneralMovimiento.TipoMovimiento.TRANSFERENCIA_RECIBIDA;
    }

    private CajaGeneralResponse mapToResponse(CajaGeneral caja, List<CajaGeneralSaldo> saldos) {
        return CajaGeneralResponse.builder()
                .id(caja.getId())
                .nombre(caja.getNombre())
                .idSucursal(caja.getIdSucursal())
                .saldoActual(caja.getSaldoActual())
                .saldosPorMetodo(saldos.stream()
                        .map(s -> CajaGeneralSaldoResponse.builder()
                                .metodoPago(s.getMetodoPago().getDescripcion())
                                .saldo(s.getSaldoActual())
                                .build())
                        .collect(Collectors.toList()))
                .fechaActualizacion(caja.getFechaActualizacion())
                .build();
    }

    private CajaGeneralMovimientoResponse mapToMovimientoResponse(CajaGeneralMovimiento mov) {
        return CajaGeneralMovimientoResponse.builder()
                .id(mov.getId())
                .tipo(mov.getTipo())
                .monto(mov.getMonto())
                .descripcion(mov.getDescripcion())
                .referencia(mov.getReferencia())
                .metodoPago(mov.getMetodoPago() != null ? mov.getMetodoPago().getDescripcion() : "EFECTIVO")
                .metodoPagoOrigen(mov.getMetodoPagoOrigen())
                .metodoPagoDestino(mov.getMetodoPagoDestino())
                .fecha(mov.getFecha())
                .usuario(mov.getUsuario())
                .build();
    }
}
