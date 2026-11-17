package com.pe.finanzas.catalogo.mapper;

import com.pe.finanzas.catalogo.entity.Catalogo;
import com.pe.finanzas.catalogo.dto.request.CreateCatalogoRequest;
import com.pe.finanzas.catalogo.dto.request.UpdateCatalogoRequest;
import com.pe.finanzas.catalogo.dto.response.CatalogoResponse;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.niveles.entity.Nivel;
import com.pe.finanzas.niveles.mapper.NivelMapper;
import com.pe.finanzas.niveles.repository.NivelRepository;
import com.pe.finanzas.catalogo.entity.TipoAtencion;
import com.pe.finanzas.catalogo.repository.TipoAtencionRepository;
import org.springframework.stereotype.Component;

@Component
public class CatalogoMapper {

    private final NivelMapper nivelMapper;
    private final NivelRepository nivelRepository;
    private final TipoAtencionRepository tipoAtencionRepository;
    private final com.pe.finanzas.catalogo.repository.TipoTemperaturaRepository tipoTemperaturaRepository;

    public CatalogoMapper(NivelMapper nivelMapper, NivelRepository nivelRepository,
            TipoAtencionRepository tipoAtencionRepository,
            com.pe.finanzas.catalogo.repository.TipoTemperaturaRepository tipoTemperaturaRepository) {
        this.nivelMapper = nivelMapper;
        this.nivelRepository = nivelRepository;
        this.tipoAtencionRepository = tipoAtencionRepository;
        this.tipoTemperaturaRepository = tipoTemperaturaRepository;
    }

    public Catalogo toEntity(CreateCatalogoRequest request) {
        if (request == null)
            return null;

        return Catalogo.builder()
                .codigo(request.getCodigo())
                .nombreServicio(request.getNombreServicio())
                .nombre(request.getNombreServicio()) // Mapear también a 'nombre' para cumplir constraint NOT NULL
                .unidad(request.getUnidad())
                .tipoUnidad(request.getTipoUnidad())
                .honorariosMedicos(request.getHonorariosMedicos())
                .tarifa(request.getTarifa())
                .precioSinIgv(request.getPrecioSinIgv())
                .precioConIgv(request.getPrecioConIgv())
                .precioCompra(request.getPrecioCompra())
                .margenGanancia(request.getMargenGanancia())
                .nivel(request.getIdNivel() != null ? nivelRepository.findById(request.getIdNivel())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Nivel no encontrado con ID: " + request.getIdNivel()))
                        : null)
                .tipoAtencion(request.getIdTipoAtencion() != null
                        ? tipoAtencionRepository.findById(request.getIdTipoAtencion())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Tipo de atención no encontrado con ID: " + request.getIdTipoAtencion()))
                        : null)
                .tipoPrecio(request.getTipoPrecio())
                .estado(request.getEstado())
                .tipo(request.getTipo())
                .tipoCatalogo(request.getTipo() != null ? request.getTipo().name() : "SERVICIO")
                .nombreCatalogo(request.getNombreServicio())
                .codigoSeus(
                        request.getCodigoSeus() != null && !request.getCodigoSeus().isBlank() ? request.getCodigoSeus()
                                : null)
                .codDigemid(
                        request.getCodDigemid() != null && !request.getCodDigemid().isBlank() ? request.getCodDigemid()
                                : null)
                .regSanitario(request.getRegSanitario())
                .idCategoria(request.getIdCategoria())
                .tipoTemperatura(request.getIdTipoTemperatura() != null
                        ? tipoTemperaturaRepository.findById(request.getIdTipoTemperatura())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Tipo de temperatura no encontrado con ID: " + request.getIdTipoTemperatura()))
                        : null)
                .moneda(request.getMoneda())
                .build();
    }

    public CatalogoResponse toResponse(Catalogo servicio) {
        if (servicio == null)
            return null;

        return CatalogoResponse.builder()
                .id(servicio.getId())
                .codigo(servicio.getCodigo())
                .nombreServicio(servicio.getNombreServicio())
                .unidad(servicio.getUnidad())
                .tipoUnidad(servicio.getTipoUnidad())
                .honorariosMedicos(servicio.getHonorariosMedicos())
                .tarifa(servicio.getTarifa())
                .precioSinIgv(servicio.getPrecioSinIgv())
                .precioConIgv(servicio.getPrecioConIgv())
                .precioCompra(servicio.getPrecioCompra())
                .margenGanancia(servicio.getMargenGanancia())
                .nivel(nivelMapper.toDto(servicio.getNivel()))
                .nivelDescripcion(
                        servicio.getNivel() != null ? servicio.getNivel().getNombre() : null)
                .idTipoAtencion(servicio.getTipoAtencion() != null ? servicio.getTipoAtencion().getId() : null)
                .tipoAtencionNombre(
                        servicio.getTipoAtencion() != null ? servicio.getTipoAtencion().getNombre() : "TODOS")
                .tipoPrecio(servicio.getTipoPrecio())
                .tipoPrecioDescripcion(
                        servicio.getTipoPrecio() != null ? servicio.getTipoPrecio().getDescripcion() : null)
                .estado(servicio.getEstado())
                // estado es Integer: 1 = Activo, 0 = Inactivo
                .estadoDescripcion(
                        servicio.getEstado() != null ? (servicio.getEstado() == 1 ? "Activo" : "Inactivo") : "Nulo")
                .tipo(servicio.getTipo())
                .tipoDescripcion(servicio.getTipo() != null ? servicio.getTipo().getDescripcion() : null)
                .codigoSeus(servicio.getCodigoSeus())
                .codDigemid(servicio.getCodDigemid())
                .regSanitario(servicio.getRegSanitario())
                .idCategoria(servicio.getIdCategoria())
                .idTipoTemperatura(servicio.getTipoTemperatura() != null ? servicio.getTipoTemperatura().getId() : null)
                .tipoTemperaturaNombre(
                        servicio.getTipoTemperatura() != null ? servicio.getTipoTemperatura().getNombre() : null)
                .tempMin(servicio.getTipoTemperatura() != null ? servicio.getTipoTemperatura().getTempMin() : null)
                .tempMax(servicio.getTipoTemperatura() != null ? servicio.getTipoTemperatura().getTempMax() : null)
                .moneda(servicio.getMoneda() != null ? servicio.getMoneda() : "PEN") // Default a PEN si es nulo
                .createdAt(servicio.getCreatedAt())
                .updatedAt(servicio.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequest(Catalogo servicio, UpdateCatalogoRequest request) {
        if (request == null || servicio == null)
            return;

        if (request.getCodigo() != null)
            servicio.setCodigo(request.getCodigo());
        if (request.getNombreServicio() != null) {
            servicio.setNombreServicio(request.getNombreServicio());
            servicio.setNombre(request.getNombreServicio()); // Mantener sincronizado con nombreServicio
        }
        if (request.getUnidad() != null)
            servicio.setUnidad(request.getUnidad());
        if (request.getTipoUnidad() != null)
            servicio.setTipoUnidad(request.getTipoUnidad());
        if (request.getHonorariosMedicos() != null)
            servicio.setHonorariosMedicos(request.getHonorariosMedicos());
        if (request.getTarifa() != null)
            servicio.setTarifa(request.getTarifa());
        if (request.getPrecioSinIgv() != null)
            servicio.setPrecioSinIgv(request.getPrecioSinIgv());
        if (request.getPrecioConIgv() != null)
            servicio.setPrecioConIgv(request.getPrecioConIgv());
        if (request.getPrecioCompra() != null)
            servicio.setPrecioCompra(request.getPrecioCompra());
        if (request.getMargenGanancia() != null)
            servicio.setMargenGanancia(request.getMargenGanancia());
        if (request.getIdNivel() != null) {
            Nivel nivel = nivelRepository.findById(request.getIdNivel())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Nivel no encontrado con ID: " + request.getIdNivel()));
            servicio.setNivel(nivel);
        }
        if (request.getIdTipoAtencion() != null) {
            TipoAtencion tipo = tipoAtencionRepository.findById(request.getIdTipoAtencion())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Tipo de atención no encontrado con ID: " + request.getIdTipoAtencion()));
            servicio.setTipoAtencion(tipo);
        } else {
            // Permitir explícitamente null para que "TODOS" funcione en actualizaciones
            servicio.setTipoAtencion(null);
        }
        if (request.getTipoPrecio() != null)
            servicio.setTipoPrecio(request.getTipoPrecio());
        if (request.getEstado() != null)
            servicio.setEstado(request.getEstado());
        if (request.getTipo() != null) {
            servicio.setTipo(request.getTipo());
            servicio.setTipoCatalogo(request.getTipo().name());
        }
        if (request.getNombreServicio() != null) {
            servicio.setNombreCatalogo(request.getNombreServicio());
        }
        if (request.getCodigoSeus() != null)
            servicio.setCodigoSeus(!request.getCodigoSeus().isBlank() ? request.getCodigoSeus() : null);
        if (request.getCodDigemid() != null)
            servicio.setCodDigemid(!request.getCodDigemid().isBlank() ? request.getCodDigemid() : null);
        if (request.getRegSanitario() != null)
            servicio.setRegSanitario(request.getRegSanitario());
        if (request.getIdCategoria() != null)
            servicio.setIdCategoria(request.getIdCategoria());
        if (request.getIdTipoTemperatura() != null) {
            com.pe.finanzas.catalogo.entity.TipoTemperatura tt = tipoTemperaturaRepository
                    .findById(request.getIdTipoTemperatura())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Tipo de temperatura no encontrado con ID: " + request.getIdTipoTemperatura()));
            servicio.setTipoTemperatura(tt);
        } else {
            servicio.setTipoTemperatura(null);
        }
        if (request.getMoneda() != null)
            servicio.setMoneda(request.getMoneda());
    }
}
