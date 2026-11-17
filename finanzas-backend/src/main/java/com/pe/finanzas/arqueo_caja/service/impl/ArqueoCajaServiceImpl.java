package com.pe.finanzas.arqueo_caja.service.impl;

import com.pe.finanzas.arqueo_caja.dto.ArqueoCajaDTO;
import com.pe.finanzas.arqueo_caja.entity.ArqueoCaja;
import com.pe.finanzas.arqueo_caja.repository.ArqueoCajaRepository;
import com.pe.finanzas.arqueo_caja.service.ArqueoCajaService;
import com.pe.finanzas.caja_chica.repository.CajaChicaRepository;
import com.pe.finanzas.puntos.repository.PuntoRepository;
import com.pe.finanzas.sucursales.repository.SucursalRepository;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArqueoCajaServiceImpl implements ArqueoCajaService {

    private final ArqueoCajaRepository arqueoRepository;
    private final com.pe.finanzas.caja_chica.repository.CajaChicaMovimientoRepository movimientoRepository;
    private final CajaChicaRepository cajaChicaRepository;
    private final DatosPersonalesRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final PuntoRepository puntoRepository;

    @Override
    public BigDecimal obtenerSaldoEsperado(Long idUsuario, Long idSucursal, Long idPunto) {
        // Find the current open caja for the user/sucursal/punto!
        Optional<com.pe.finanzas.caja_chica.entity.CajaChica> openCaja = Optional.empty();
        
        if (idUsuario != null) {
            openCaja = cajaChicaRepository.findOpenBoxNative(
                String.valueOf(idUsuario), 
                String.valueOf(idUsuario), 
                idPunto, 
                idSucursal
            );
        }
        
        // If we have an open caja, use its saldoActual or calculate from its last arqueo!
        if (openCaja.isPresent()) {
            // Find last arqueo for this sucursal/punto (if any)
            Optional<ArqueoCaja> ultimoCierre;
            if (idPunto != null) {
                ultimoCierre = arqueoRepository.findFirstByIdSucursalAndIdPuntoOrderByFechaHoraDesc(idSucursal, idPunto);
            } else {
                ultimoCierre = arqueoRepository.findFirstByIdSucursalOrderByFechaHoraDesc(idSucursal);
            }
            
            LocalDateTime inicio = ultimoCierre.map(ArqueoCaja::getFechaHora)
                .orElse(openCaja.get().getCreatedAt());
            LocalDateTime fin = LocalDateTime.now();
            
            BigDecimal ingresos = movimientoRepository.sumIngresosByCajaAndRange(openCaja.get().getId(), inicio, fin);
            BigDecimal egresos = movimientoRepository.sumEgresosByCajaAndRange(openCaja.get().getId(), inicio, fin);
            
            ingresos = (ingresos != null) ? ingresos : BigDecimal.ZERO;
            egresos = (egresos != null) ? egresos : BigDecimal.ZERO;
            
            return openCaja.get().getSaldoInicial().add(ingresos).subtract(egresos);
        }
        
        // Fallback if no open caja
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public ArqueoCajaDTO registrarArqueo(ArqueoCajaDTO dto) {
        log.info("Registrando arqueo de caja para sucursal: {}, punto: {}, usuario: {}", dto.getIdSucursal(), dto.getIdPunto(), dto.getIdUsuario());

        ArqueoCaja entidad = ArqueoCaja.builder()
                .fechaHora(LocalDateTime.now())
                .idUsuario(dto.getIdUsuario())
                .idSucursal(dto.getIdSucursal())
                .idPunto(dto.getIdPunto())
                .saldoEsperado(dto.getSaldoEsperado())
                .saldoFisico(dto.getSaldoFisico())
                .diferencia(dto.getSaldoFisico().subtract(dto.getSaldoEsperado()))
                .observaciones(dto.getObservaciones())
                .estado("CERRADO")
                .build();

        entidad = arqueoRepository.save(entidad);
        
        // Also, close the caja if we have it!
        Optional<com.pe.finanzas.caja_chica.entity.CajaChica> openCaja = Optional.empty();
        if (dto.getIdUsuario() != null) {
            openCaja = cajaChicaRepository.findOpenBoxNative(
                String.valueOf(dto.getIdUsuario()), 
                String.valueOf(dto.getIdUsuario()), 
                dto.getIdPunto(), 
                dto.getIdSucursal()
            );
            if (openCaja.isPresent()) {
                openCaja.get().setEstado(com.pe.finanzas.caja_chica.entity.CajaChica.EstadoCaja.CERRADA);
                openCaja.get().setSaldoCierreReal(dto.getSaldoFisico());
                openCaja.get().setFechaCierre(LocalDateTime.now());
                cajaChicaRepository.save(openCaja.get());
            }
        }
        
        return mapToDTO(entidad);
    }

    @Override
    public Page<ArqueoCajaDTO> listarArqueos(Long idUsuario, Long idSucursal, Long idPunto, LocalDateTime start, LocalDateTime end,
            Pageable pageable) {
        Page<ArqueoCaja> pagina;
        
        if (idPunto != null) {
            if (start != null && end != null) {
                pagina = arqueoRepository.findByIdSucursalAndIdPuntoAndFechaHoraBetweenOrderByFechaHoraDesc(idSucursal, idPunto, start, end,
                        pageable);
            } else {
                pagina = arqueoRepository.findByIdSucursalAndIdPuntoOrderByFechaHoraDesc(idSucursal, idPunto, pageable);
            }
        } else {
            if (start != null && end != null) {
                pagina = arqueoRepository.findByIdSucursalAndFechaHoraBetweenOrderByFechaHoraDesc(idSucursal, start, end,
                        pageable);
            } else {
                pagina = arqueoRepository.findByIdSucursalOrderByFechaHoraDesc(idSucursal, pageable);
            }
        }
        
        return pagina.map(this::mapToDTO);
    }

    private ArqueoCajaDTO mapToDTO(ArqueoCaja entidad) {
        String nombreUsuario = "N/A";
        if (entidad.getIdUsuario() != null) {
            nombreUsuario = usuarioRepository.findById(entidad.getIdUsuario())
                    .map(u -> (u.getFullName() != null && !u.getFullName().trim().isEmpty()) ? u.getFullName()
                            : u.getLogin())
                    .orElse("ID: " + entidad.getIdUsuario());
        }

        String nombreSucursal = "N/A";
        if (entidad.getIdSucursal() != null) {
            nombreSucursal = sucursalRepository.findById(entidad.getIdSucursal())
                    .map(s -> s.getNombreSucursal())
                    .orElse("ID: " + entidad.getIdSucursal());
        }

        String nombrePunto = "N/A";
        if (entidad.getIdPunto() != null) {
            nombrePunto = puntoRepository.findById(entidad.getIdPunto())
                    .map(p -> p.getNombre())
                    .orElse("ID: " + entidad.getIdPunto());
        }

        return ArqueoCajaDTO.builder()
                .id(entidad.getId())
                .fechaHora(entidad.getFechaHora())
                .idUsuario(entidad.getIdUsuario())
                .nombreUsuario(nombreUsuario)
                .idSucursal(entidad.getIdSucursal())
                .nombreSucursal(nombreSucursal)
                .idPunto(entidad.getIdPunto())
                .nombrePunto(nombrePunto)
                .saldoEsperado(entidad.getSaldoEsperado())
                .saldoFisico(entidad.getSaldoFisico())
                .diferencia(entidad.getDiferencia())
                .observaciones(entidad.getObservaciones())
                .estado(entidad.getEstado())
                .build();
    }
}
