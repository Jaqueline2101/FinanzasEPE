package com.pe.finanzas.caja_chica.services;

import com.pe.finanzas.caja_chica.dto.*;
import com.pe.finanzas.caja_chica.entity.CajaChica;
import com.pe.finanzas.caja_chica.entity.CajaChicaMovimiento;
import com.pe.finanzas.caja_chica.repository.CajaChicaMovimientoRepository;
import com.pe.finanzas.caja_chica.repository.CajaChicaRepository;
import com.pe.finanzas.atributos.repository.MetodoPagoRepository;
import com.pe.finanzas.atributos.entity.MetodoPago;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CajaChicaService {

        private final CajaChicaRepository cajaChicaRepository;
        private final CajaChicaMovimientoRepository movimientoRepository;
        private final MetodoPagoRepository metodoPagoRepository;
        private final com.pe.finanzas.caja_general.service.CajaGeneralService cajaGeneralService;

        @Transactional(readOnly = true)
        public List<CajaChicaResponse> listarCajas() {
                return cajaChicaRepository.findAll().stream()
                                .map(this::mapToCajaResponse)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
    public java.util.Optional<CajaChicaResponse> obtenerCajaAbierta(Long idPuntoVenta, Long idSucursal, String usuario, String username) {
        if (usuario == null && username == null) return java.util.Optional.empty();
        
        // Usamos una consulta NATIVA SQL para evitar cualquier problema de mapeo de Hibernate
        return cajaChicaRepository.findOpenBoxNative(
            usuario != null ? usuario.trim() : null, 
            username != null ? username.trim() : null,
            idPuntoVenta,
            idSucursal
        ).map(this::mapToCajaResponse);
    }

        @Transactional(readOnly = true)
        public java.util.Optional<CajaChica> obtenerCajaAbiertaEntidad(String usuario) {
            return cajaChicaRepository.findFirstByEstadoAndIdUsuarioCajeroOrderByIdDesc(
                    CajaChica.EstadoCaja.ABIERTA, usuario);
        }

        @Transactional
        public CajaChicaResponse crearCaja(CajaChicaRequest request, String usuario) {
                // Validar que no haya una caja abierta para este punto de venta y usuario
                boolean existeAbierta = cajaChicaRepository.findAll().stream()
                                .anyMatch(c -> c.getEstado() == CajaChica.EstadoCaja.ABIERTA
                                                && c.getIdPuntoVenta().equals(request.getIdPuntoVenta())
                                                && c.getIdUsuarioCajero().equals(usuario));

                if (existeAbierta) {
                        throw new RuntimeException("Ya existe una caja abierta para este punto de venta y usuario");
                }

                // Validar que el usuario no haya tenido ya una caja hoy (así esté cerrada)
                if (cajaChicaRepository.existsCajaTodayForUser(usuario)) {
                        throw new RuntimeException("Ya has abierto una caja hoy. Solo se permite una caja por día por usuario.");
                }

                BigDecimal saldoInicial = request.getSaldoInicial() != null ? request.getSaldoInicial()
                                : BigDecimal.ZERO;

                CajaChica caja = CajaChica.builder()
                                .nombre(request.getNombre())
                                .idSucursal(request.getIdSucursal())
                                .idPuntoVenta(request.getIdPuntoVenta())
                                .idUsuarioCajero(usuario)
                                .saldoInicial(saldoInicial)
                                .saldoActual(BigDecimal.ZERO)
                                .estado(CajaChica.EstadoCaja.ABIERTA)
                                .usuarioCreacion(usuario)
                                .build();

                CajaChica savedCaja = cajaChicaRepository.save(caja);

                // Registrar el saldo inicial como el primer movimiento si es > 0
                if (saldoInicial.compareTo(BigDecimal.ZERO) > 0) {
                        registrarMovimientoInterno(savedCaja, CajaChicaMovimiento.TipoMovimiento.INGRESO, saldoInicial, "FONDO INICIAL AL ABRIR CAJA", "APERTURA", "EFECTIVO", usuario);
                }

                return mapToCajaResponse(savedCaja);
        }

        @Transactional
    public MovimientoResponse registrarMovimiento(MovimientoRequest request, String usuario) {
        CajaChica caja;

        if (request.getCajaChicaId() != null) {
            caja = cajaChicaRepository.findById(request.getCajaChicaId())
                    .orElseThrow(() -> new RuntimeException("Caja chica no encontrada"));
        } else {
            // Si no viene ID, buscamos la caja abierta (solo por usuario para retrocompatibilidad)
            caja = cajaChicaRepository.findOpenBoxNative(usuario, usuario, null, null)
                    .orElseThrow(() -> new RuntimeException("No hay una caja abierta. Debe abrir una caja desde Ingresos y Gastos antes de registrar movimientos"));
        }

                return registrarMovimientoInterno(caja, request.getTipo(), request.getMonto(), 
                        request.getDescripcion(), request.getReferencia(), request.getMetodoPago(), usuario);
        }

        @Transactional
        public MovimientoResponse registrarMovimientoInterno(CajaChica caja, CajaChicaMovimiento.TipoMovimiento tipo, 
                BigDecimal monto, String descripcion, String referencia, String metodoPagoDesc, String usuario) {
            
            if (caja.getEstado() == CajaChica.EstadoCaja.CERRADA) {
                throw new RuntimeException("La caja chica está cerrada");
            }

            CajaChicaMovimiento movimiento = CajaChicaMovimiento.builder()
                    .cajaChica(caja)
                    .tipo(tipo)
                    .monto(monto)
                    .descripcion(descripcion)
                    .referencia(referencia)
                    .metodoPago(metodoPagoRepository.findByDescripcionIgnoreCase(
                            metodoPagoDesc != null ? metodoPagoDesc : "EFECTIVO")
                            .orElseGet(() -> {
                                // Si no existe, lo creamos dinámicamente (como hicimos en Caja General)
                                MetodoPago nuevo = MetodoPago.builder()
                                        .descripcion(metodoPagoDesc != null ? metodoPagoDesc.toUpperCase() : "EFECTIVO")
                                        .estado(1)
                                        .build();
                                return metodoPagoRepository.save(nuevo);
                            }))
                    .fecha(LocalDateTime.now())
                    .usuario(usuario)
                    .build();

            // Actualizar saldo
            if (tipo == CajaChicaMovimiento.TipoMovimiento.INGRESO) {
                caja.setSaldoActual(caja.getSaldoActual().add(monto));
            } else {
                caja.setSaldoActual(caja.getSaldoActual().subtract(monto));
            }

            cajaChicaRepository.save(caja);
            return mapToMovimientoResponse(movimientoRepository.save(movimiento));
        }

        @Transactional(readOnly = true)
        public Page<MovimientoResponse> listarMovimientos(Long cajaId, Pageable pageable) {
                return movimientoRepository.findByCajaChicaIdOrderByFechaDesc(cajaId, pageable)
                                .map(this::mapToMovimientoResponse);
        }

        @Transactional(readOnly = true)
        public CajaResumenDTO obtenerResumen(Long id) {
                CajaChica caja = cajaChicaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Caja chica no encontrada"));

                List<CajaChicaMovimiento> movimientos = movimientoRepository.findByCajaChicaId(id);

                Map<String, BigDecimal> ingresosPorMetodo = movimientos.stream()
                                .filter(m -> m.getTipo() == CajaChicaMovimiento.TipoMovimiento.INGRESO)
                                .collect(Collectors.groupingBy(
                                                m -> m.getMetodoPago() != null ? m.getMetodoPago().getDescripcion()
                                                                : "EFECTIVO",
                                                Collectors.reducing(BigDecimal.ZERO, CajaChicaMovimiento::getMonto,
                                                                BigDecimal::add)));

                Map<String, BigDecimal> egresosPorMetodo = movimientos.stream()
                                .filter(m -> m.getTipo() == CajaChicaMovimiento.TipoMovimiento.EGRESO)
                                .collect(Collectors.groupingBy(
                                                m -> m.getMetodoPago() != null ? m.getMetodoPago().getDescripcion()
                                                                : "EFECTIVO",
                                                Collectors.reducing(BigDecimal.ZERO, CajaChicaMovimiento::getMonto,
                                                                BigDecimal::add)));

                List<CajaResumenDTO.MetodoPagoResumenDTO> ingresosDTO = new ArrayList<>();
                ingresosPorMetodo.forEach((k, v) -> ingresosDTO.add(new CajaResumenDTO.MetodoPagoResumenDTO(k, v)));

                List<CajaResumenDTO.MetodoPagoResumenDTO> egresosDTO = new ArrayList<>();
                egresosPorMetodo.forEach((k, v) -> egresosDTO.add(new CajaResumenDTO.MetodoPagoResumenDTO(k, v)));

                BigDecimal totalIngresos = ingresosDTO.stream().map(CajaResumenDTO.MetodoPagoResumenDTO::getMonto)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalEgresos = egresosDTO.stream().map(CajaResumenDTO.MetodoPagoResumenDTO::getMonto)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return CajaResumenDTO.builder()
                                .cajaId(caja.getId())
                                .nombre(caja.getNombre())
                                .saldoInicial(caja.getSaldoInicial())
                                .ingresos(ingresosDTO)
                                .egresos(egresosDTO)
                                .totalIngresos(totalIngresos)
                                .totalEgresos(totalEgresos)
                                .saldoFinalTeorico(caja.getSaldoActual())
                                .build();
        }

        @Transactional
        public CajaChicaResponse cerrarCaja(Long id, BigDecimal saldoCierreReal, boolean transferirACajaGeneral,
                        String usuario) {
                CajaChica caja = cajaChicaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Caja chica no encontrada"));

                if (caja.getEstado() == CajaChica.EstadoCaja.CERRADA) {
                        throw new RuntimeException("La caja ya se encuentra cerrada");
                }

                caja.setEstado(CajaChica.EstadoCaja.CERRADA);
                caja.setSaldoCierreReal(saldoCierreReal);
                caja.setFechaCierre(LocalDateTime.now());

                if (transferirACajaGeneral && saldoCierreReal.compareTo(BigDecimal.ZERO) > 0) {
                        cajaGeneralService.registrarMovimiento(
                                        com.pe.finanzas.caja_general.dto.CajaGeneralMovimientoRequest.builder()
                                                        .idSucursal(caja.getIdSucursal())
                                                        .tipo(com.pe.finanzas.caja_general.entity.CajaGeneralMovimiento.TipoMovimiento.TRANSFERENCIA_RECIBIDA)
                                                        .monto(saldoCierreReal)
                                                        .descripcion("TRANSFERENCIA POR CIERRE DE CAJA CHICA: " + caja.getNombre())
                                                        .referencia("ID_CAJA_CHICA:" + caja.getId())
                                                        .metodoPago("EFECTIVO")
                                                        .usuario(usuario)
                                                        .build());
                }

                return mapToCajaResponse(cajaChicaRepository.save(caja));
        }

        private CajaChicaResponse mapToCajaResponse(CajaChica caja) {
                return CajaChicaResponse.builder()
                                .id(caja.getId())
                                .nombre(caja.getNombre())
                                .saldoActual(caja.getSaldoActual())
                                .estado(caja.getEstado())
                                .saldoInicial(caja.getSaldoInicial())
                                .saldoCierreReal(caja.getSaldoCierreReal())
                                .idPuntoVenta(caja.getIdPuntoVenta())
                                .idUsuarioCajero(caja.getIdUsuarioCajero())
                                .fechaCierre(caja.getFechaCierre())
                                .createdAt(caja.getCreatedAt())
                                .build();
        }

        private MovimientoResponse mapToMovimientoResponse(CajaChicaMovimiento mov) {
                return MovimientoResponse.builder()
                                .id(mov.getId())
                                .tipo(mov.getTipo())
                                .monto(mov.getMonto())
                                .descripcion(mov.getDescripcion())
                                .referencia(mov.getReferencia())
                                .metodoPago(mov.getMetodoPago() != null ? mov.getMetodoPago().getDescripcion() : null)
                                .fecha(mov.getFecha())
                                .usuario(mov.getUsuario())
                                .build();
        }
}
