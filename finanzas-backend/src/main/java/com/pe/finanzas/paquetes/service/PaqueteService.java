package com.pe.finanzas.paquetes.service;

import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.paquetes.dto.PaqueteDto;
import com.pe.finanzas.paquetes.dto.PaqueteRequest;
import com.pe.finanzas.paquetes.entity.Paquete;
import com.pe.finanzas.paquetes.entity.PaqueteDetalle;
import com.pe.finanzas.paquetes.repository.PaqueteDetalleRepository;
import com.pe.finanzas.paquetes.repository.PaqueteRepository;
import com.pe.finanzas.catalogo.repository.CatalogoRepository;
import com.pe.finanzas.catalogo.repository.TipoAtencionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Paquetes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaqueteService {

        private final PaqueteRepository paqueteRepository;
        private final PaqueteDetalleRepository paqueteDetalleRepository;
        private final CatalogoRepository servicioRepository;
        private final TipoAtencionRepository tipoAtencionRepository;
        private final com.pe.finanzas.service.KafkaProducerService kafkaProducerService;

        /**
         * Crear nuevo paquete
         */
        @Transactional
        public PaqueteDto crearPaquete(PaqueteRequest request) {
                log.info("Creando paquete: {}", request.getNombre());

                Paquete paquete = Paquete.builder()
                                .codigo(request.getCodigo())
                                .nombre(request.getNombre())
                                .descripcion(request.getDescripcion())
                                .idEmpresa(request.getIdEmpresa())
                                .tipoAtencionEntity(request.getIdTipoAtencion() != null
                                                ? tipoAtencionRepository.findById(request.getIdTipoAtencion())
                                                                .orElse(null)
                                                : null)
                                .tipoAtencion(request.getIdTipoAtencion() != null
                                                ? tipoAtencionRepository.findById(request.getIdTipoAtencion())
                                                                .map(com.pe.finanzas.catalogo.entity.TipoAtencion::getNombre)
                                                                .orElse(request.getTipoAtencion())
                                                : request.getTipoAtencion())
                                .precioPaquete(request.getPrecioPaquete())
                                .descuento(request.getDescuento())
                                .fechaInicio(request.getFechaInicio())
                                .fechaFin(request.getFechaFin())
                                .estado(request.getEstado() != null ? request.getEstado() : "1")
                                .categoria(request.getCategoria() != null ? request.getCategoria() : "PAQUETE_REGULAR")
                                .build();

                Paquete guardado = paqueteRepository.save(paquete);

                // Guardar servicios si existen
                if (request.getServicios() != null && !request.getServicios().isEmpty()) {
                        guardarServicios(guardado, request.getServicios());
                }

                return convertirADto(guardado);
        }

        /**
         * Actualizar paquete existente
         */
        @Transactional
        public PaqueteDto actualizarPaquete(Long id, PaqueteRequest request) {
                log.info("Actualizando paquete ID: {}", id);

                Paquete paquete = paqueteRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Paquete no encontrado con ID: " + id));

                paquete.setCodigo(request.getCodigo());
                paquete.setNombre(request.getNombre());
                paquete.setDescripcion(request.getDescripcion());
                paquete.setIdEmpresa(request.getIdEmpresa());
                paquete.setTipoAtencionEntity(request.getIdTipoAtencion() != null
                                ? tipoAtencionRepository.findById(request.getIdTipoAtencion()).orElse(null)
                                : null);

                if (paquete.getTipoAtencionEntity() != null) {
                        paquete.setTipoAtencion(paquete.getTipoAtencionEntity().getNombre());
                } else {
                        paquete.setTipoAtencion(request.getTipoAtencion());
                }
                paquete.setPrecioPaquete(request.getPrecioPaquete());
                paquete.setDescuento(request.getDescuento());
                paquete.setFechaInicio(request.getFechaInicio());
                paquete.setFechaFin(request.getFechaFin());

                if (request.getCategoria() != null) {
                        paquete.setCategoria(request.getCategoria());
                }

                if (request.getEstado() != null) {
                        paquete.setEstado(request.getEstado());
                }

                Paquete actualizado = paqueteRepository.save(paquete);

                // Actualizar servicios (Lógica Merge para evitar Unique Constraint Violation)
                if (request.getServicios() != null) {
                        List<PaqueteRequest.ServicioRequest> serviciosRequest = request.getServicios();
                        List<PaqueteDetalle> detallesActuales = actualizado.getDetalles();

                        // 1. Eliminar los que ya no están en el request
                        detallesActuales.removeIf(detalle -> serviciosRequest.stream()
                                        .noneMatch(req -> req.getIdServicio().equals(detalle.getServicio().getId())));

                        // 2. Actualizar existentes y agregar nuevos
                        for (PaqueteRequest.ServicioRequest req : serviciosRequest) {
                                PaqueteDetalle detalleExistente = detallesActuales.stream()
                                                .filter(d -> d.getServicio().getId().equals(req.getIdServicio()))
                                                .findFirst()
                                                .orElse(null);

                                if (detalleExistente != null) {
                                        // Actualizar
                                        detalleExistente.setCantidad(
                                                        req.getCantidad() != null
                                                                        ? java.math.BigDecimal
                                                                                        .valueOf(req.getCantidad())
                                                                        : java.math.BigDecimal.ONE);
                                } else {
                                        // Agregar nuevo
                                        PaqueteDetalle nuevoDetalle = PaqueteDetalle.builder()
                                                        .paquete(actualizado)
                                                        .servicio(servicioRepository.findById(req.getIdServicio())
                                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                                        "Servicio no encontrado con ID: "
                                                                                                        + req.getIdServicio())))
                                                        .cantidad(
                                                                        req.getCantidad() != null ? java.math.BigDecimal
                                                                                        .valueOf(req.getCantidad())
                                                                                        : java.math.BigDecimal.ONE)
                                                        .build();
                                        detallesActuales.add(nuevoDetalle);
                                }
                        }

                        // Guardar nuevamente
                        actualizado = paqueteRepository.save(actualizado);
                }

                return convertirADto(actualizado);
        }

        /**
         * Obtener paquete por ID
         */
        @Transactional(readOnly = true)
        public PaqueteDto obtenerPaquete(Long id) {
                Paquete paquete = paqueteRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Paquete no encontrado con ID: " + id));
                return convertirADto(paquete);
        }

        /**
         * Listar todos los paquetes (paginado)
         */
        @Transactional(readOnly = true)
        public Page<PaqueteDto> listarPaquetes(Pageable pageable) {
                return paqueteRepository.findAll(pageable)
                                .map(this::convertirADto);
        }

        /**
         * Listar paquetes por categoría (paginado)
         */
        @Transactional(readOnly = true)
        public Page<PaqueteDto> listarPaquetesPorCategoria(String categoria, Pageable pageable) {
                if (categoria == null || categoria.isEmpty()) {
                        return listarPaquetes(pageable);
                }
                return paqueteRepository.findByCategoria(categoria, pageable)
                                .map(this::convertirADto);
        }

        /**
         * Listar paquetes activos
         */
        @Transactional(readOnly = true)
        public List<PaqueteDto> listarPaquetesActivos() {
                return paqueteRepository.findByEstado("1").stream()
                                .map(this::convertirADto)
                                .collect(Collectors.toList());
        }

        /**
         * Listar paquetes vigentes
         */
        @Transactional(readOnly = true)
        public List<PaqueteDto> listarPaquetesVigentes() {
                LocalDate hoy = LocalDate.now();
                return paqueteRepository.findAll().stream()
                                .filter(p -> p.isVigente(hoy))
                                .map(this::convertirADto)
                                .collect(Collectors.toList());
        }

        /**
         * Cambiar estado del paquete
         */
        @Transactional
        public void cambiarEstado(Long id, String estado) {
                Paquete paquete = paqueteRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Paquete no encontrado con ID: " + id));

                paquete.setEstado(estado);
                paqueteRepository.save(paquete);
        }

        /**
         * Eliminar paquete
         */
        @Transactional
        public void eliminarPaquete(Long id) {
                Paquete paquete = paqueteRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Paquete no encontrado con ID: " + id));

                // Soft delete: Change status to '0' (Inactive)
                paquete.setEstado("0");
                paqueteRepository.save(paquete);

                log.info("Paquete desactivado (soft delete) ID: {}", id);
        }

        /**
         * Verificar y desactivar paquetes vencidos
         * Se llama desde el Scheduler diario
         */
        @Transactional
        public void verificarVencimientoPaquetes() {
                log.info("Verificando vencimiento de paquetes...");
                LocalDate hoy = LocalDate.now();

                List<Paquete> vencidos = paqueteRepository.findByEstadoAndFechaFinBefore("1", hoy);

                if (!vencidos.isEmpty()) {
                        for (Paquete p : vencidos) {
                                p.setEstado("0");
                                log.info("Desactivando paquete vencido: {} - Fin: {}", p.getNombre(), p.getFechaFin());

                                // Notificar desactivación
                                kafkaProducerService.enviarNotificacion(
                                                null,
                                                "PAQUETES",
                                                "Paquete Desactivado (Expirado)",
                                                "El paquete '" + p.getNombre()
                                                                + "' ha vencido y fue desactivado automáticamente.",
                                                "error");
                        }
                        paqueteRepository.saveAll(vencidos);
                        log.info("Se desactivaron {} paquetes vencidos.", vencidos.size());
                } else {
                        log.info("No se encontraron paquetes vencidos para desactivar.");
                }
        }

        // ========== MÉTODOS AUXILIARES ==========

        private void guardarServicios(Paquete paquete, List<PaqueteRequest.ServicioRequest> servicios) {
                for (PaqueteRequest.ServicioRequest servicioReq : servicios) {
                        PaqueteDetalle detalle = PaqueteDetalle.builder()
                                        .paquete(paquete)
                                        .servicio(servicioRepository.findById(servicioReq.getIdServicio())
                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                        "Servicio no encontrado con ID: "
                                                                                        + servicioReq.getIdServicio())))
                                        .cantidad(
                                                        servicioReq.getCantidad() != null
                                                                        ? java.math.BigDecimal.valueOf(
                                                                                        servicioReq.getCantidad())
                                                                        : java.math.BigDecimal.ONE)
                                        .descuentoPorcentaje(servicioReq.getDescuentoPorcentaje() != null
                                                        ? servicioReq.getDescuentoPorcentaje()
                                                        : BigDecimal.ZERO)
                                        .build();
                        paqueteDetalleRepository.save(detalle);
                }
        }

        private PaqueteDto convertirADto(Paquete entity) {
                // Cargar servicios
                List<PaqueteDto.ServicioPaqueteDto> servicios = paqueteDetalleRepository
                                .findByPaqueteIdOrdenado(entity.getIdPaquete())
                                .stream()
                                .map(this::convertirServicioADto)
                                .collect(Collectors.toList());

                boolean permanente = (entity.getFechaInicio() == null && entity.getFechaFin() == null);

                return PaqueteDto.builder()
                                .idPaquete(entity.getIdPaquete())
                                .codigo(entity.getCodigo())
                                .nombre(entity.getNombre())
                                .descripcion(entity.getDescripcion())
                                .idEmpresa(entity.getIdEmpresa())
                                .idTipoAtencion(entity.getTipoAtencionEntity() != null
                                                ? entity.getTipoAtencionEntity().getId()
                                                : null)
                                .tipoAtencionNombre(entity.getTipoAtencionEntity() != null
                                                ? entity.getTipoAtencionEntity().getNombre()
                                                : entity.getTipoAtencion())
                                .tipoAtencion(entity.getTipoAtencion())
                                .precioPaquete(entity.getPrecioPaquete())
                                .descuento(entity.getDescuento())
                                .fechaInicio(entity.getFechaInicio())
                                .fechaFin(entity.getFechaFin())
                                .estado(entity.getEstado())
                                .activo(entity.isActivo())
                                .vigente(entity.isVigenteHoy())
                                .permanente(permanente)
                                .categoria(entity.getCategoria())
                                .servicios(servicios)
                                .build();
        }

        private PaqueteDto.ServicioPaqueteDto convertirServicioADto(PaqueteDetalle entity) {
                String nombreServicio = "Servicio desconocido";
                Long idServicio = null;
                BigDecimal precioUnitario = BigDecimal.ZERO;

                if (entity.getServicio() != null) {
                        nombreServicio = entity.getServicio().getNombreServicio();
                        idServicio = entity.getServicio().getId();
                        precioUnitario = entity.getServicio().getPrecioConIgv() != null
                                        ? entity.getServicio().getPrecioConIgv()
                                        : BigDecimal.ZERO;
                }

                // Calcular valores de descuento
                BigDecimal descuentoPorcentaje = entity.getDescuentoPorcentaje() != null
                                ? entity.getDescuentoPorcentaje()
                                : BigDecimal.ZERO;

                BigDecimal precioReal = precioUnitario.multiply(
                                entity.getCantidad() != null ? entity.getCantidad() : BigDecimal.ONE);

                BigDecimal descuentoMonto = precioReal.multiply(descuentoPorcentaje)
                                .divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);

                BigDecimal precioFinal = precioReal.subtract(descuentoMonto);

                return PaqueteDto.ServicioPaqueteDto.builder()
                                .idDetalle(entity.getIdPaqueteDetalle())
                                .idServicio(idServicio)
                                .nombreServicio(nombreServicio)
                                .cantidad(entity.getCantidad() != null ? entity.getCantidad().intValue() : 1)
                                .precioUnitario(precioUnitario)
                                .descuentoPorcentaje(descuentoPorcentaje)
                                .descuentoMonto(descuentoMonto)
                                .precioFinal(precioFinal)
                                .build();
        }
}
