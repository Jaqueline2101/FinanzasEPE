package com.pe.finanzas.puntos.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.puntos.dto.PuntoDto;
import com.pe.finanzas.puntos.dto.PuntosDocumentoDto;
import com.pe.finanzas.puntos.entity.Punto;
import com.pe.finanzas.puntos.repository.PuntoRepository;
import com.pe.finanzas.puntos.service.PuntoService;
import com.pe.finanzas.sucursales.entity.Sucursal;
import com.pe.finanzas.sucursales.repository.SucursalRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PuntoServiceImpl implements PuntoService {

    private final PuntoRepository puntoRepository;
    private final SucursalRepository sucursalRepository;

    @Override
    public PuntoDto crear(PuntoDto puntoDto) {
        log.info("Creando punto de venta: {}", puntoDto.getNombre());

        // Validar que no exista el nombre en la misma sucursal
        if (puntoRepository.existsByNombreAndSucursalIdSucursal(
                puntoDto.getNombre(), puntoDto.getIdSucursal())) {
            throw new IllegalArgumentException(
                    "Ya existe un punto con el nombre '" + puntoDto.getNombre() +
                            "' en esta sucursal");
        }

        // Obtener sucursal
        Sucursal sucursal = sucursalRepository.findById(puntoDto.getIdSucursal())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal no encontrada con ID: " + puntoDto.getIdSucursal()));

        Punto punto = Punto.builder()
                .nombre(puntoDto.getNombre())
                .sucursal(sucursal)
                .idSucursal(puntoDto.getIdSucursal() != null ? puntoDto.getIdSucursal().intValue() : null)
                .tipo(puntoDto.getTipo() != null ? puntoDto.getTipo() : "F") // F=Finanzas
                .valido(puntoDto.getValido() != null ? puntoDto.getValido() : "1")
                .imprimeComp(puntoDto.getImprimeComp() != null ? puntoDto.getImprimeComp() : "1")
                .agrupaServ(puntoDto.getAgrupaServ() != null ? puntoDto.getAgrupaServ() : "1")
                .buscaXIniciales(puntoDto.getBuscaXIniciales() != null ? puntoDto.getBuscaXIniciales() : "1")
                .cargaMedico(puntoDto.getCargaMedico() != null ? puntoDto.getCargaMedico() : "1")
                .cargaCuenta(puntoDto.getCargaCuenta() != null ? puntoDto.getCargaCuenta() : 1)
                .cobra(puntoDto.getCobra() != null ? puntoDto.getCobra() : "0")
                .ipAccesoModulo(puntoDto.getIpAccesoModulo() != null ? puntoDto.getIpAccesoModulo() : "0.0.0.0")
                .modulo(puntoDto.getModulo() != null ? puntoDto.getModulo() : 50)
                .arqueo(puntoDto.getArqueo() != null ? puntoDto.getArqueo() : "0")
                .fe(puntoDto.getFe() != null ? puntoDto.getFe() : "0")
                .build();

        punto = puntoRepository.save(punto);
        log.info("Punto de venta creado con ID: {}", punto.getPunto());

        return convertirADto(punto);
    }

    @Override
    public PuntoDto actualizar(Long id, PuntoDto puntoDto) {
        log.info("Actualizando punto de venta ID: {}", id);

        Punto punto = puntoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Punto no encontrado con ID: " + id));

        // Validar nombre único en sucursal si cambió
        if (!punto.getNombre().equals(puntoDto.getNombre())) {
            if (puntoRepository.existsByNombreAndSucursalIdSucursal(
                    puntoDto.getNombre(), punto.getSucursal().getIdSucursal())) {
                throw new IllegalArgumentException(
                        "Ya existe un punto con el nombre '" + puntoDto.getNombre() +
                                "' en esta sucursal");
            }
        }

        // punto.setPunto(puntoDto.getPunto()); // El ID no debe cambiarse manualmente
        // así si es autoincremental
        punto.setNombre(puntoDto.getNombre());
        punto.setTipo(puntoDto.getTipo());
        punto.setValido(puntoDto.getValido());
        punto.setIdSucursal(puntoDto.getIdSucursal() != null ? puntoDto.getIdSucursal().intValue() : null);

        punto = puntoRepository.save(punto);
        log.info("Punto de venta actualizado: {}", id);

        return convertirADto(punto);
    }

    @Override
    @Transactional(readOnly = true)
    public PuntoDto obtenerPorId(Long id) {
        Punto punto = puntoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Punto no encontrado con ID: " + id));
        return convertirADto(punto);
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando punto de venta ID: {}", id);

        Punto punto = puntoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Punto no encontrado con ID: " + id));

        punto.setValido("0");
        puntoRepository.save(punto);
        log.info("Punto de venta desactivado: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PuntoDto> listarTodos(Pageable pageable) {
        return puntoRepository.findAll(pageable)
                .map(this::convertirADto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDto> listarActivos() {
        return puntoRepository.findByActivoTrue().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDto> listarPorSucursal(Long idSucursal) {
        return puntoRepository.findBySucursalIdSucursal(idSucursal).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PuntoDto> listarPorSucursal(Long idSucursal, Pageable pageable) {
        return puntoRepository.findBySucursalIdSucursal(idSucursal, pageable)
                .map(this::convertirADto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDto> listarActivosPorSucursal(Long idSucursal) {
        return puntoRepository.findBySucursalIdSucursalAndActivoTrue(idSucursal).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PuntoDto> buscarPorNombre(String nombre, Pageable pageable) {
        return puntoRepository.searchByNombre(nombre, pageable)
                .map(this::convertirADto);
    }

    @Override
    public void cambiarEstado(Long id, Boolean activo) {
        log.info("Cambiando estado del punto ID: {} a {}", id, activo);

        Punto punto = puntoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Punto no encontrado con ID: " + id));

        punto.setValido(activo ? "1" : "0");
        puntoRepository.save(punto);
        log.info("Estado del punto actualizado: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombreYSucursal(String nombrePunto, Long idSucursal) {
        return puntoRepository.existsByNombreAndSucursalIdSucursal(nombrePunto, idSucursal);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorSucursal(Long idSucursal) {
        return puntoRepository.countBySucursalIdSucursal(idSucursal);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarActivosPorSucursal(Long idSucursal) {
        return puntoRepository.countBySucursalIdSucursalAndActivoTrue(idSucursal);
    }

    // Métodos de conversión
    private PuntoDto convertirADto(Punto entity) {
        List<PuntosDocumentoDto> documentosDto = entity.getDocumentos().stream()
                .map(this::convertirDocumentoADto)
                .collect(Collectors.toList());

        return PuntoDto.builder()
                .idPunto(entity.getPunto())
                .punto(entity.getPunto() != null ? entity.getPunto().toString() : null)
                .nombre(entity.getNombre())
                .tipo(entity.getTipo())
                .valido(entity.getValido())
                .idSucursal(entity.getSucursal().getIdSucursal())
                .nombreSucursal(entity.getSucursal().getNombreSucursal())
                .imprimeComp(entity.getImprimeComp())
                .agrupaServ(entity.getAgrupaServ())
                .buscaXIniciales(entity.getBuscaXIniciales())
                .cargaMedico(entity.getCargaMedico())
                .cargaCuenta(entity.getCargaCuenta())
                .cobra(entity.getCobra())
                .ipAccesoModulo(entity.getIpAccesoModulo())
                .modulo(entity.getModulo())
                .arqueo(entity.getArqueo())
                .fe(entity.getFe())
                .documentos(documentosDto)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PuntosDocumentoDto convertirDocumentoADto(com.pe.finanzas.puntos.entity.PuntosDocumento entity) {
        return PuntosDocumentoDto.builder()
                .idPuntoDocumento(entity.getId())
                .idTipoDocumento(entity.getTipoDocumento().getIdTipoDocumento())
                .codigoTipoDocumento(entity.getTipoDocumento().getCodigo())
                .nombreTipoDocumento(entity.getTipoDocumento().getNombre())
                .serie(entity.getSerie())
                .numeroActual(entity.getNumeroActual())
                .siguienteNumero(entity.obtenerSiguienteNumero())
                .activo(entity.getActivo())
                .build();
    }
}
