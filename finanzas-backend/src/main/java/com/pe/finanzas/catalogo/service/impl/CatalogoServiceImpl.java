package com.pe.finanzas.catalogo.service.impl;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.exception.ConflictException;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.catalogo.dto.request.CreateCatalogoRequest;
import com.pe.finanzas.catalogo.dto.request.UpdateCatalogoRequest;
import com.pe.finanzas.catalogo.dto.response.CatalogoResponse;
import com.pe.finanzas.catalogo.entity.Catalogo;
import com.pe.finanzas.catalogo.mapper.CatalogoMapper;
import com.pe.finanzas.catalogo.repository.CatalogoRepository;
import com.pe.finanzas.catalogo.service.CatalogoService;
import com.pe.finanzas.shared.dto.PageResponse;
import com.pe.finanzas.catalogo.dto.response.DescuentoConfiguradoDto;
import com.pe.finanzas.motor_precios.repository.CredPlanDescuentoRepository;
import com.pe.finanzas.motor_precios.entity.CredPlanDescuento;
import com.pe.finanzas.niveles.repository.NivelRepository;
import com.pe.finanzas.catalogo.repository.TipoAtencionRepository;
import com.pe.finanzas.niveles.dto.NivelDto;
import com.pe.finanzas.motor_precios.entity.CredPlan;
import com.pe.finanzas.motor_precios.repository.CredPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {

    private static final BigDecimal IGV_RATE = new BigDecimal("1.18");

    private final CatalogoRepository servicioRepository;
    private final CatalogoMapper servicioMapper;
    private final com.pe.finanzas.descuentos.service.AplicacionDescuentoService aplicacionDescuentoService;
    private final CredPlanDescuentoRepository credPlanDescuentoRepository;
    private final NivelRepository nivelRepository;
    private final TipoAtencionRepository tipoAtencionRepository;
    private final CredPlanRepository credPlanRepository;

    private static final List<Catalogo.Tipo> LISTABLE_TYPES = List.of(Catalogo.Tipo.SERVICIO, Catalogo.Tipo.PAQUETE);

    @Override
    @Transactional
    public CatalogoResponse createServicio(CreateCatalogoRequest request) {
        log.info("Creating servicio with name: {}", request.getNombreServicio());

        if (request.getCodigo() != null && !request.getCodigo().isEmpty()) {
            if (servicioRepository.existsByCodigo(request.getCodigo())) {
                throw new ConflictException("Ya existe un servicio con el código: " + request.getCodigo());
            }
        }

        if (request.getPrecioConIgv() == null && request.getPrecioSinIgv() != null) {
            BigDecimal precioConIgv = request.getPrecioSinIgv().multiply(IGV_RATE);
            request.setPrecioConIgv(precioConIgv);
        }

        validarPrecios(request.getPrecioSinIgv(), request.getPrecioConIgv());
        validarLogicaPrecios(request.getPrecioCompra(), request.getPrecioConIgv(), request.getMargenGanancia());

        Catalogo servicio = servicioMapper.toEntity(request);
        Catalogo savedServicio = servicioRepository.save(servicio);

        log.info("Servicio created successfully with ID: {}", savedServicio.getId());
        return servicioMapper.toResponse(savedServicio);
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogoResponse getServicioById(Long id) {
        log.info("Fetching servicio with ID: {}", id);

        Catalogo servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Servicio no encontrado con ID: " + id));

        return servicioMapper.toResponse(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogoResponse getServicioByCodigo(String codigo) {
        log.info("Fetching servicio with codigo: {}", codigo);

        Catalogo servicio = servicioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Servicio no encontrado con código: " + codigo));

        return servicioMapper.toResponse(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CatalogoResponse> getAllServicios(Pageable pageable) {
        log.info("Fetching all servicios with pageable: {}", pageable);

        Page<Catalogo> serviciosPage = servicioRepository.findByTipoIn(LISTABLE_TYPES, pageable);

        List<CatalogoResponse> content = serviciosPage.getContent().stream()
                .map(servicioMapper::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<CatalogoResponse>(
                content,
                serviciosPage.getNumber(),
                serviciosPage.getSize(),
                serviciosPage.getTotalElements(),
                serviciosPage.getTotalPages(),
                serviciosPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CatalogoResponse> getServiciosByEstado(Integer estado, Pageable pageable) {
        log.info("Fetching servicios by estado: {}", estado);
        Page<Catalogo> page = servicioRepository.findByEstadoAndTipoIn(estado, LISTABLE_TYPES, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CatalogoResponse> getServiciosByTipoAtencion(Long idTipoAtencion, Pageable pageable) {
        log.info("Fetching servicios by tipo atencion ID: {}", idTipoAtencion);
        Page<Catalogo> page = servicioRepository.findByTipoAtencion_IdAndTipoIn(idTipoAtencion, LISTABLE_TYPES, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CatalogoResponse> searchServicios(String searchTerm, Pageable pageable) {

        return searchServiciosConContexto(searchTerm, null, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CatalogoResponse> searchServiciosConContexto(String searchTerm, Long idConvenio,
            String tipoCliente, Pageable pageable) {
        log.info("Searching servicios with context - term: {}, convenio: {}, tipo: {}", searchTerm, idConvenio,
                tipoCliente);

        String term = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : "";

        Page<Catalogo> serviciosPage;
        if (term.isEmpty()) {
            serviciosPage = servicioRepository.findByTipoIn(LISTABLE_TYPES, pageable);
        } else {
            serviciosPage = servicioRepository.searchServicios(term, LISTABLE_TYPES, pageable);
        }

        List<CatalogoResponse> content = serviciosPage.getContent().stream()
                .map(servicio -> {
                    CatalogoResponse response = servicioMapper.toResponse(servicio);

                    if (idConvenio != null) {
                        Long idNivel = servicio.getNivel() != null ? servicio.getNivel().getIdNivel() : null;
                        aplicacionDescuentoService.obtenerDescuentoPorPlan(
                                idConvenio,
                                servicio.getId(),
                                idNivel).ifPresent(descuento -> {
                                    BigDecimal precioBase = response.getPrecioConIgv() != null
                                            ? response.getPrecioConIgv()
                                            : BigDecimal.ZERO;

                                    BigDecimal porcentaje = descuento.getPorcentajeDescuento() != null
                                            ? descuento.getPorcentajeDescuento()
                                            : BigDecimal.ZERO;

                                    BigDecimal montoFijo = descuento.getDescuentoFijo() != null
                                            ? descuento.getDescuentoFijo()
                                            : BigDecimal.ZERO;

                                    BigDecimal nuevoPrecio = precioBase;
                                    if (porcentaje.compareTo(BigDecimal.ZERO) > 0) {
                                        BigDecimal factor = porcentaje.divide(new BigDecimal("100"), 4,
                                                RoundingMode.HALF_UP);
                                        nuevoPrecio = precioBase.subtract(precioBase.multiply(factor)).setScale(2,
                                                RoundingMode.HALF_UP);
                                    } else if (montoFijo.compareTo(BigDecimal.ZERO) > 0) {
                                        nuevoPrecio = precioBase.subtract(montoFijo).setScale(2, RoundingMode.HALF_UP);
                                    }

                                    response.setPrecioConvenio(nuevoPrecio);
                                    response.setPorcentajeDescuento(porcentaje);
                                    response.setMontoDescuentoFijo(montoFijo);
                                });
                    }

                    return response;
                })
                .collect(Collectors.toList());

        return new PageResponse<CatalogoResponse>(
                content,
                serviciosPage.getNumber(),
                serviciosPage.getSize(),
                serviciosPage.getTotalElements(),
                serviciosPage.getTotalPages(),
                serviciosPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CatalogoResponse> searchServiciosByEstado(String searchTerm, Integer estado,
            Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getServiciosByEstado(estado, pageable);
        }
        Page<Catalogo> page = servicioRepository.searchServiciosByEstado(searchTerm.trim(), estado, LISTABLE_TYPES, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional
    public CatalogoResponse updateServicio(Long id, UpdateCatalogoRequest request) {
        Catalogo servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        if (request.getCodigo() != null && servicioRepository.existsByCodigoAndIdNot(request.getCodigo(), id)) {
            throw new ConflictException("El código ya está en uso por otro servicio");
        }

        BigDecimal sinIgv = request.getPrecioSinIgv() != null ? request.getPrecioSinIgv() : servicio.getPrecioSinIgv();
        BigDecimal conIgv = request.getPrecioConIgv() != null ? request.getPrecioConIgv() : servicio.getPrecioConIgv();
        BigDecimal pCompra = request.getPrecioCompra() != null ? request.getPrecioCompra() : servicio.getPrecioCompra();
        BigDecimal margen = request.getMargenGanancia() != null ? request.getMargenGanancia()
                : servicio.getMargenGanancia();

        validarPrecios(sinIgv, conIgv);
        validarLogicaPrecios(pCompra, conIgv, margen);

        servicioMapper.updateEntityFromRequest(servicio, request);
        return servicioMapper.toResponse(servicioRepository.save(servicio));
    }

    @Override
    @Transactional
    public void deleteServicio(Long id) {
        log.info("Deleting (soft-delete) servicio with ID: {}", id);

        Catalogo servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        // Al usar delete, Hibernate ejecutará el @SQLDelete definido en la entidad (deleted = true)
        servicioRepository.delete(servicio);

        log.info("Servicio logically deleted (deleted = true) with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public com.pe.finanzas.catalogo.dto.response.SmartSearchResponse searchSmart(String searchTerm, Long idConvenio,
            String tipoCliente) {
        log.info("Smart Search - term: {}, convenio initial: {}", searchTerm, idConvenio);
        String term = (searchTerm != null) ? searchTerm.trim() : "";

        com.pe.finanzas.catalogo.dto.response.SmartSearchResponse.SmartSearchResponseBuilder responseBuilder = com.pe.finanzas.catalogo.dto.response.SmartSearchResponse
                .builder();

        List<com.pe.finanzas.catalogo.dto.response.SmartSearchResponse.SearchResultItem> items = new java.util.ArrayList<>();

        if (!term.isEmpty() && idConvenio == null) {
            Optional<com.pe.finanzas.motor_precios.entity.CredPlan> planOpt = credPlanRepository.findAll().stream()
                    .filter(p -> p.getCodigo() != null && p.getCodigo().equalsIgnoreCase(term) && p.isActivo())
                    .findFirst();

            if (planOpt.isPresent()) {
                var plan = planOpt.get();
                responseBuilder.planMatch(com.pe.finanzas.catalogo.dto.response.SmartSearchResponse.PlanMatch.builder()
                        .idPlan(plan.getIdPlan())
                        .codigo(plan.getCodigo())
                        .nombre(plan.getNombre())
                        .tipoPlan(plan.getTipoPlan())
                        .build());
                idConvenio = plan.getIdPlan();
            }
        }

        final Long finalIdConvenio = idConvenio;
        final boolean isPlanActive = finalIdConvenio != null;
        
        // PROGRAMADOR SENIOR: Cargar el plan una sola vez fuera del bucle para máximo rendimiento
        Optional<CredPlan> planActivoOpt = isPlanActive ? credPlanRepository.findById(finalIdConvenio) : Optional.empty();

        List<Catalogo> catalogos = new java.util.ArrayList<>();

        // PROGRAMADOR SENIOR: Siempre buscamos en todo el catálogo para no ocultar servicios al cajero
        if (term.isEmpty()) {
            catalogos = servicioRepository.findByTipoIn(LISTABLE_TYPES, org.springframework.data.domain.PageRequest.of(0, 10))
                    .getContent();
        } else {
            catalogos = servicioRepository
                    .searchServicios(term, LISTABLE_TYPES, org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        }

        List<com.pe.finanzas.catalogo.dto.response.SmartSearchResponse.SearchResultItem> catalogItems = catalogos.stream()
                .map(svc -> {
                    // PROGRAMADOR SENIOR: Asegurar que el precio real nunca sea nulo
                    BigDecimal pReal = svc.getPrecioConIgv();
                    if (pReal == null || pReal.compareTo(BigDecimal.ZERO) == 0) {
                        pReal = svc.getPrecioSinIgv() != null ? svc.getPrecioSinIgv() : BigDecimal.ZERO;
                    }
                    
                    BigDecimal pFinal = pReal;
                    BigDecimal porcDesc = BigDecimal.ZERO;
                    boolean hasDesc = false;
                    boolean hasRule = false;

                    if (isPlanActive) {
                        Long idNivel = svc.getNivel() != null ? svc.getNivel().getIdNivel() : null;
                        var descOpt = aplicacionDescuentoService.obtenerDescuentoPorPlan(finalIdConvenio,
                                svc.getId(), idNivel);

                        if (descOpt.isPresent()) {
                            hasRule = true;
                            var desc = descOpt.get();
                            
                            // 1. Prioridad: Precio Fijo (Tarifa Plana)
                            if (desc.getPrecioFijo() != null && desc.getPrecioFijo().compareTo(BigDecimal.ZERO) >= 0) {
                                pFinal = desc.getPrecioFijo();
                                hasDesc = true;
                                if (pReal.compareTo(BigDecimal.ZERO) > 0) {
                                    porcDesc = BigDecimal.valueOf(100).subtract(
                                        pFinal.multiply(BigDecimal.valueOf(100)).divide(pReal, 2, RoundingMode.HALF_UP)
                                    );
                                }
                            } 
                            // 2. Segunda Prioridad: Descuento Porcentual
                            else if (desc.getPorcentajeDescuento() != null && desc.getPorcentajeDescuento().compareTo(BigDecimal.ZERO) > 0) {
                                porcDesc = desc.getPorcentajeDescuento();
                                BigDecimal factor = porcDesc.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                                pFinal = pReal.subtract(pReal.multiply(factor)).setScale(2, RoundingMode.HALF_UP);
                                hasDesc = true;
                            }
                            // 3. Tercera Prioridad: Descuento Fijo
                            else if (desc.getDescuentoFijo() != null && desc.getDescuentoFijo().compareTo(BigDecimal.ZERO) > 0) {
                                BigDecimal dFijo = desc.getDescuentoFijo();
                                pFinal = pReal.subtract(dFijo).setScale(2, RoundingMode.HALF_UP);
                                if (pReal.compareTo(BigDecimal.ZERO) > 0) {
                                    porcDesc = dFijo.multiply(BigDecimal.valueOf(100)).divide(pReal, 2, RoundingMode.HALF_UP);
                                }
                                hasDesc = true;
                            }
                        } else if (planActivoOpt.isPresent()) {
                            // PROGRAMADOR SENIOR: Fallback al Coaseguro del Plan si no hay regla específica
                            CredPlan p = planActivoOpt.get();
                            if (p.getPorcentajeCoaseguro() != null && p.getPorcentajeCoaseguro().compareTo(BigDecimal.ZERO) > 0) {
                                // El paciente paga el porcentaje de coaseguro (ej: 20%)
                                pFinal = pReal.multiply(p.getPorcentajeCoaseguro().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);
                                porcDesc = BigDecimal.valueOf(100).subtract(p.getPorcentajeCoaseguro());
                                hasDesc = true;
                            }
                        }
                    }

                    return com.pe.finanzas.catalogo.dto.response.SmartSearchResponse.SearchResultItem.builder()
                            .id(svc.getId())
                            .codigo(svc.getCodigo())
                            .nombre(svc.getNombreServicio() != null ? svc.getNombreServicio() : svc.getNombre())
                            .tipo(svc.getTipo() != null ? svc.getTipo().name() : "SERVICIO")
                            .precioReal(pReal)
                            .precioFinal(pFinal)
                            .porcentajeDescuento(porcDesc)
                            .tieneDescuento(hasDesc)
                            .hasRule(hasRule)
                            .nivelNombre(svc.getNivel() != null ? svc.getNivel().getNombre() : "")
                            .tipoCatalogo(svc.getTipoCatalogo())
                            .build();
                })
                .collect(Collectors.toList());

        items.addAll(catalogItems);
        responseBuilder.results(items);
        return responseBuilder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoResponse> getAllServiciosList() {
        log.info("Fetching all servicios as list");

        return servicioRepository.findByTipoIn(LISTABLE_TYPES, Pageable.unpaged()).getContent().stream()
                .map(servicioMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validarPrecios(BigDecimal sinIgv, BigDecimal conIgv) {
        if (sinIgv != null && conIgv != null && conIgv.compareTo(sinIgv) < 0) {
            throw new BadRequestException("El precio con IGV no puede ser menor al precio sin IGV");
        }
    }

    private void validarLogicaPrecios(BigDecimal precioCompra, BigDecimal precioConIgv, BigDecimal margenGanancia) {
        if (precioCompra == null || precioConIgv == null || margenGanancia == null) {
            return;
        }

        try {
            BigDecimal costoNeto = precioCompra.divide(IGV_RATE, 4, java.math.RoundingMode.HALF_UP);
            BigDecimal valorVentaNeto = precioConIgv.divide(IGV_RATE, 4, java.math.RoundingMode.HALF_UP);

            if (valorVentaNeto.compareTo(BigDecimal.ZERO) == 0)
                return;

            BigDecimal ganancia = valorVentaNeto.subtract(costoNeto);
            BigDecimal margenCalculado = ganancia.divide(valorVentaNeto, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            BigDecimal diferencia = margenCalculado.subtract(margenGanancia).abs();

            if (diferencia.compareTo(new BigDecimal("1.00")) > 0) { // Tolerancia 1%
                log.warn("Margen inconsistente. Esperado: {}, Calculado: {}", margenGanancia, margenCalculado);

            }
        } catch (Exception e) {
            log.error("Error validando precios", e);
        }
    }

    private PageResponse<CatalogoResponse> mapToPageResponse(Page<Catalogo> page) {
        List<CatalogoResponse> content = page.getContent().stream()
                .map(servicioMapper::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<CatalogoResponse>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DescuentoConfiguradoDto> getDescuentosPorPlan(Long idPlan) {
        log.info("Fetching descuentos for plan ID: {}", idPlan);

        return credPlanDescuentoRepository.findActivosByPlan(idPlan).stream()
                .map(this::mapToDescuentoConfiguradoDto)
                .collect(Collectors.toList());
    }

    private DescuentoConfiguradoDto mapToDescuentoConfiguradoDto(CredPlanDescuento entity) {
        DescuentoConfiguradoDto.DescuentoConfiguradoDtoBuilder builder = DescuentoConfiguradoDto.builder()
                .idPlanDescuento(entity.getIdPlanDescuento())
                .idServicio(entity.getIdServicio())
                .tipoDescuento(entity.getTipoDescuento())
                .codigoPlan(entity.getCodigoPlan())
                .estado(entity.getEstado());

        BigDecimal descuentoFijo = BigDecimal.ZERO;
        BigDecimal descuentoPorcentaje = BigDecimal.ZERO;
        BigDecimal precioConIgv = BigDecimal.ZERO;

        if (entity.getIdServicio() != null) {
            Optional<Catalogo> servicioOpt = servicioRepository.findById(entity.getIdServicio());
            if (servicioOpt.isPresent()) {
                Catalogo servicio = servicioOpt.get();
                builder.servicio(servicioMapper.toResponse(servicio));
                precioConIgv = servicio.getPrecioConIgv() != null ? servicio.getPrecioConIgv() : BigDecimal.ZERO;
            }

            if (entity.esMontoFijo()) {
                descuentoFijo = entity.getDescuento() != null ? entity.getDescuento() : BigDecimal.ZERO;
                if (precioConIgv.compareTo(BigDecimal.ZERO) > 0) {
                    descuentoPorcentaje = descuentoFijo.divide(precioConIgv, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")).setScale(2, java.math.RoundingMode.HALF_UP);
                }
            } else if (entity.esPorcentaje()) {
                descuentoPorcentaje = entity.getDescuento() != null ? entity.getDescuento() : BigDecimal.ZERO;
                if (precioConIgv.compareTo(BigDecimal.ZERO) > 0) {
                    descuentoFijo = precioConIgv.multiply(descuentoPorcentaje).divide(new BigDecimal("100"), 2,
                            java.math.RoundingMode.HALF_UP);
                }
            }
        } else {
            descuentoFijo = entity.getDescuentoFijo() != null ? entity.getDescuentoFijo() : BigDecimal.ZERO;
            descuentoPorcentaje = entity.getDescuentoPorcentaje() != null ? entity.getDescuentoPorcentaje()
                    : BigDecimal.ZERO;

            if (descuentoFijo.compareTo(BigDecimal.ZERO) == 0
                    && descuentoPorcentaje.compareTo(BigDecimal.ZERO) == 0
                    && entity.getDescuento() != null) {
                if (entity.esMontoFijo())
                    descuentoFijo = entity.getDescuento();
                else
                    descuentoPorcentaje = entity.getDescuento();
            }
        }

        builder.descuentoFijo(descuentoFijo);
        builder.descuentoPorcentaje(descuentoPorcentaje);
        builder.precioFijo(entity.getPrecioFijo() != null ? entity.getPrecioFijo()
                : ("PRECIO_FIJO".equals(entity.getTipoDescuento()) ? entity.getDescuento() : null));

        if (entity.getNivelServicio() != null && entity.getNivelServicio() > 0) {
            builder.idNivel(entity.getNivelServicio());
            try {
                nivelRepository.findById(Long.valueOf(entity.getNivelServicio())).ifPresent(nivel -> {
                    NivelDto dto = new NivelDto();
                    dto.setIdNivel(nivel.getIdNivel());
                    dto.setNombre(nivel.getNombre());
                    builder.nivel(dto);
                });
            } catch (Exception e) {
                log.warn("Error mapping nivel for discount: {}", e.getMessage());
            }
        }

        if (entity.getTipoAtencion() != null && !entity.getTipoAtencion().equals("TODOS")) {
            try {
                tipoAtencionRepository.findByNombre(entity.getTipoAtencion()).ifPresent(tipo -> {
                    builder.tipoAtencion(new DescuentoConfiguradoDto.TipoAtencionSimpleDto(tipo.getId(),
                            tipo.getNombre(), tipo.getCodigo()));
                });
            } catch (Exception e) {
                log.warn("Error mapping tipo atencion: {}", e.getMessage());
            }
        }

        return builder.build();
    }

    @Override
    @Transactional
    public void aplicarDescuento(com.pe.finanzas.catalogo.dto.request.AplicarDescuentoDto request) {
        log.info("Aplicando descuento: Plan={}, TipoApp={}", request.getIdPlan(), request.getTipoAplicacion());

        com.pe.finanzas.motor_precios.entity.CredPlan plan = credPlanRepository.findById(request.getIdPlan())
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado con ID: " + request.getIdPlan()));

        if ("SERVICIO".equals(request.getTipoAplicacion())) {

            if (request.getIdServicio() == null) {
                throw new BadRequestException("ID de servicio es requerido para aplicación por SERVICIO");
            }
            crearOActualizarRegla(plan, request.getIdServicio(), request.getIdNivel(), null, request);

        } else if ("NIVEL".equals(request.getTipoAplicacion())) {

            if (request.getIdNivel() == null) {
                throw new BadRequestException("ID de nivel es requerido para aplicación por NIVEL");
            }

            String tipoAtencionNombre = "TODOS";
            if (request.getIdTipoAtencion() != null) {
                tipoAtencionNombre = tipoAtencionRepository.findById(request.getIdTipoAtencion())
                        .map(com.pe.finanzas.catalogo.entity.TipoAtencion::getNombre)
                        .orElse("TODOS");
            }

            crearOActualizarRegla(plan, null, Integer.valueOf(request.getIdNivel().intValue()), tipoAtencionNombre,
                    request);

            List<com.pe.finanzas.niveles.entity.Nivel> hijos = nivelRepository
                    .findByNivelPadreIdNivelOrderByOrden(Long.valueOf(request.getIdNivel()));
            for (com.pe.finanzas.niveles.entity.Nivel hijo : hijos) {
                crearOActualizarRegla(plan, null, Integer.valueOf(hijo.getIdNivel().intValue()), tipoAtencionNombre,
                        request);
            }

        } else if ("TODOS".equals(request.getTipoAplicacion())) {
            String tipoAtencionNombre = "TODOS";
            if (request.getIdTipoAtencion() != null) {
                tipoAtencionNombre = tipoAtencionRepository.findById(request.getIdTipoAtencion())
                        .map(com.pe.finanzas.catalogo.entity.TipoAtencion::getNombre)
                        .orElse("TODOS");
            }

            credPlanDescuentoRepository.deleteByPlanIdPlan(plan.getIdPlan());
            crearOActualizarRegla(plan, null, 0, tipoAtencionNombre, request);
        }
    }

    private void crearOActualizarRegla(
            com.pe.finanzas.motor_precios.entity.CredPlan plan,
            Long idServicio,
            Integer idNivel,
            String tipoAtencion,
            com.pe.finanzas.catalogo.dto.request.AplicarDescuentoDto request) {

        Optional<CredPlanDescuento> existente = Optional.empty();

        if (idServicio != null) {
            existente = credPlanDescuentoRepository.findByPlanAndServicio(plan.getIdPlan(), idServicio);
        } else {

            List<CredPlanDescuento> reglas = credPlanDescuentoRepository.findByPlanTipoAtencionAndNivel(
                    plan.getIdPlan(),
                    tipoAtencion,
                    idNivel != null ? idNivel : 0);

            Integer targetLevel = idNivel != null ? idNivel : 0;
            existente = reglas.stream()
                    .filter(r -> {
                        Integer rNivel = r.getNivelServicio() != null ? r.getNivelServicio() : 0;
                        return rNivel.equals(targetLevel);
                    })
                    .findFirst();
        }

        CredPlanDescuento regla;
        if (existente.isPresent()) {
            regla = existente.get();
            regla.setCodigoPlan(plan.getCodigo());

            actualizarValoresRegla(regla, request);
        } else {

            regla = new CredPlanDescuento();
            regla.setPlan(plan);
            regla.setCodigoPlan(plan.getCodigo());
            regla.setIdServicio(idServicio);
            regla.setNivelServicio(idNivel != null ? idNivel : 0);
            regla.setTipoAtencion(tipoAtencion != null ? tipoAtencion : "TODOS");
            regla.setEstado("1");
            actualizarValoresRegla(regla, request);
        }
        credPlanDescuentoRepository.save(regla);
    }

    private void actualizarValoresRegla(CredPlanDescuento regla,
            com.pe.finanzas.catalogo.dto.request.AplicarDescuentoDto request) {

        regla.setDescuentoFijo(request.getDescuentoFijo() != null ? request.getDescuentoFijo() : BigDecimal.ZERO);
        regla.setDescuentoPorcentaje(
                request.getDescuentoPorcentaje() != null ? request.getDescuentoPorcentaje() : BigDecimal.ZERO);
        regla.setPrecioFijo(request.getPrecioFijo());

        if (request.getTipoDescuento() != null && !request.getTipoDescuento().isEmpty()) {
            String tipo = request.getTipoDescuento().toUpperCase();
            if (tipo.equals("TARIFA_PLANA"))
                tipo = "PRECIO_FIJO";
            if (tipo.equals("FIJO"))
                tipo = "MONTO_FIJO";

            regla.setTipoDescuento(tipo);
            if ("PRECIO_FIJO".equals(tipo)) {
                regla.setDescuento(request.getPrecioFijo());
            } else if ("MONTO_FIJO".equals(tipo)) {
                regla.setDescuento(regla.getDescuentoFijo());
            } else {
                regla.setDescuento(regla.getDescuentoPorcentaje());
            }
        } else {
            if (request.getPrecioFijo() != null && request.getPrecioFijo().compareTo(BigDecimal.ZERO) >= 0) {
                regla.setDescuento(request.getPrecioFijo());
                regla.setTipoDescuento("PRECIO_FIJO");
            } else if (request.getDescuentoFijo() != null
                    && request.getDescuentoFijo().compareTo(BigDecimal.ZERO) > 0) {
                regla.setDescuento(request.getDescuentoFijo());
                regla.setTipoDescuento("MONTO_FIJO");
            } else {
                regla.setDescuento(
                        request.getDescuentoPorcentaje() != null ? request.getDescuentoPorcentaje() : BigDecimal.ZERO);
                regla.setTipoDescuento("PORCENTAJE");
            }
        }
    }
}
