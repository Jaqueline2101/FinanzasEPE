package com.pe.finanzas.motor_precios.service;

import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.motor_precios.dto.BulkVinculationRequest;
import com.pe.finanzas.motor_precios.dto.ConvenioDto;
import com.pe.finanzas.motor_precios.dto.ConvenioRequest;
import com.pe.finanzas.motor_precios.dto.DescuentoConvenioDto;
import com.pe.finanzas.motor_precios.entity.CredPlan;
import com.pe.finanzas.motor_precios.entity.CredPlanDescuento;
import com.pe.finanzas.compania.entity.Compania;
import com.pe.finanzas.motor_precios.repository.CredPlanDescuentoRepository;
import com.pe.finanzas.motor_precios.repository.CredPlanRepository;
import com.pe.finanzas.compania.repository.CompaniaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConvenioService {

    private final CredPlanRepository credPlanRepository;
    private final CredPlanDescuentoRepository credPlanDescuentoRepository;
    private final CompaniaRepository companiaRepository;
    private final com.pe.finanzas.empresa.repository.EmpresaRepository empresaRepository;

    @Transactional
    public ConvenioDto crearConvenio(ConvenioRequest request) {
        log.info("Creando convenio: {}", request.getNombre());

        actualizarDatosCompania(request);

        CredPlan convenio = CredPlan.builder()
                .nombre(request.getNombre())
                .codigo(request.getCodigo())
                .idCompania(request.getIdCompania())
                .idEmpresa(request.getIdEmpresa()) // Added
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .estado(request.getEstado() != null ? request.getEstado() : "1")
                .poliza(request.getPoliza())
                .controlPreex(request.getControlPreex())
                .idPersonalUser(request.getIdPersonalUser())
                .fechaIni(request.getFechaIni() != null ? request.getFechaIni() : request.getFechaInicio())
                .abrev(request.getAbrev())
                .numCuotas(request.getNumCuotas())
                .contador(request.getContador())
                .detalle(request.getDetalle())
                .correlativo(request.getCorrelativo())
                .np(request.getNp())
                .independiente(request.getIndependiente())
                .paquete(request.getPaquete())
                .planNuevo(request.getPlanNuevo())
                .tipoFact(request.getTipoFact())
                .contador2(request.getContador2())
                .mostrarEmerg(request.getMostrarEmerg())
                .segViaje(request.getSegViaje())
                .chequeo(request.getChequeo())
                .ggss(request.getGgss())
                .codpro(request.getCodpro())
                // Campos de seguro
                .copago(request.getCopago())
                .deducible(request.getDeducible())
                .porcentajeCoaseguro(request.getPorcentajeCoaseguro())
                .topeAnual(request.getTopeAnual())
                .tieneDeducible(request.getTieneDeducible() != null ? request.getTieneDeducible() : "0")
                .build();

        CredPlan guardado = credPlanRepository.save(convenio);

        if (request.getDescuentos() != null && !request.getDescuentos().isEmpty()) {
            guardarDescuentos(guardado, request.getDescuentos());
        }

        return convertirADto(guardado);
    }

    @Transactional
    public ConvenioDto actualizarConvenio(Long id, ConvenioRequest request) {
        log.info("Actualizando convenio ID: {}", id);

        CredPlan convenio = credPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convenio no encontrado con ID: " + id));

        actualizarDatosCompania(request);

        convenio.setNombre(request.getNombre());
        convenio.setCodigo(request.getCodigo());
        convenio.setIdCompania(request.getIdCompania());
        convenio.setIdEmpresa(request.getIdEmpresa());
        convenio.setFechaInicio(request.getFechaInicio());
        convenio.setFechaFin(request.getFechaFin());

        convenio.setPoliza(request.getPoliza());
        convenio.setControlPreex(request.getControlPreex());
        convenio.setIdPersonalUser(request.getIdPersonalUser());
        convenio.setFechaIni(request.getFechaIni() != null ? request.getFechaIni() : request.getFechaInicio());
        convenio.setAbrev(request.getAbrev());
        convenio.setNumCuotas(request.getNumCuotas());
        convenio.setContador(request.getContador());
        convenio.setDetalle(request.getDetalle());
        convenio.setCorrelativo(request.getCorrelativo());
        convenio.setNp(request.getNp());
        convenio.setIndependiente(request.getIndependiente());
        convenio.setPaquete(request.getPaquete());
        convenio.setPlanNuevo(request.getPlanNuevo());
        convenio.setTipoFact(request.getTipoFact());
        convenio.setContador2(request.getContador2());
        convenio.setMostrarEmerg(request.getMostrarEmerg());
        convenio.setSegViaje(request.getSegViaje());
        convenio.setChequeo(request.getChequeo());
        convenio.setGgss(request.getGgss());
        convenio.setCodpro(request.getCodpro());

        // Campos de seguro
        convenio.setCopago(request.getCopago());
        convenio.setDeducible(request.getDeducible());
        convenio.setPorcentajeCoaseguro(request.getPorcentajeCoaseguro());
        convenio.setTopeAnual(request.getTopeAnual());
        if (request.getTieneDeducible() != null) {
            convenio.setTieneDeducible(request.getTieneDeducible());
        }

        if (request.getEstado() != null) {
            convenio.setEstado(request.getEstado());
        }

        CredPlan actualizado = credPlanRepository.save(convenio);

        if (request.getDescuentos() != null) {
            credPlanDescuentoRepository.deleteByPlanIdPlan(id);
            credPlanDescuentoRepository.flush();

            if (!request.getDescuentos().isEmpty()) {
                guardarDescuentos(actualizado, request.getDescuentos());
            }
        }

        return convertirADto(actualizado);
    }

    private void actualizarDatosCompania(ConvenioRequest request) {
        if (request.getIdCompania() != null) {
            Compania compania = companiaRepository.findById(request.getIdCompania())
                    .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada"));

            boolean updated = false;
            if (request.getCodigo() != null && !request.getCodigo().isEmpty()) {
                compania.setCodigo(request.getCodigo());
                updated = true;
            }
            if (request.getRepresentante() != null && !request.getRepresentante().isEmpty()) {
                compania.setRepresentante(request.getRepresentante());
                updated = true;
            }
            if (request.getCodeps() != null && !request.getCodeps().isEmpty()) {
                compania.setCodEps(request.getCodeps());
                updated = true;
            }
            if (request.getCtacte() != null) {
                compania.setCtaCte(request.getCtacte());
                updated = true;
            }

            if (updated) {
                companiaRepository.save(compania);
            }
        }
    }

    @Transactional(readOnly = true)
    public ConvenioDto obtenerConvenio(Long id) {
        CredPlan convenio = credPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convenio no encontrado con ID: " + id));
        return convertirADto(convenio);
    }

    @Transactional(readOnly = true)
    public Page<ConvenioDto> listarConvenios(Pageable pageable) {
        return credPlanRepository.findAll(pageable)
                .map(this::convertirADto);
    }

    @Transactional(readOnly = true)
    public List<ConvenioDto> listarPlanesPorCompania(Long idCompania) {
        return credPlanRepository.findByCompania(idCompania).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConvenioDto> listarConveniosActivos() {
        return credPlanRepository.findAllPlans().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConvenioDto> listarConveniosVigentes() {
        LocalDate hoy = LocalDate.now();
        return credPlanRepository.findAll().stream()
                .filter(c -> c.isVigente(hoy))
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConvenioDto obtenerPorCodigo(String codigo) {
        log.info("Buscando convenio por código: {}", codigo);
        CredPlan convenio = credPlanRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Convenio no encontrado con código: " + codigo));
        return convertirADto(convenio);
    }

    @Transactional
    public void cambiarEstado(Long id, String estado) {
        CredPlan convenio = credPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convenio no encontrado con ID: " + id));

        convenio.setEstado(estado);
        credPlanRepository.save(convenio);
    }

    @Transactional
    public void eliminarConvenio(Long id) {
        if (!credPlanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Convenio no encontrado con ID: " + id);
        }
        credPlanRepository.deleteById(id);
    }

    @Transactional
    public DescuentoConvenioDto agregarRegla(Long idPlan, ConvenioRequest.DescuentoRequest request) {
        CredPlan plan = credPlanRepository.findById(idPlan)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado con ID: " + idPlan));

        CredPlanDescuento.CredPlanDescuentoBuilder builder = CredPlanDescuento.builder()
                .plan(plan)
                .idServicio(request.getIdServicio())
                .descuento(request.getPorcentajeDescuento())
                .tipoDescuento(request.getTipoDescuento() != null ? request.getTipoDescuento() : "PORCENTAJE")
                .estado(request.getEstado() != null ? request.getEstado() : "1");

        if (request.getIdServicio() != null) {
            builder.tipoAtencion(null);
            builder.nivelServicio(null);
        } else {
            builder.tipoAtencion(request.getTipoAtencion());
            builder.nivelServicio(request.getNivelServicio());
        }

        CredPlanDescuento descuento = builder.build();

        CredPlanDescuento guardado = credPlanDescuentoRepository.save(descuento);
        return convertirDescuentoADto(guardado);
    }

    @Transactional
    public void eliminarRegla(Long idPlan, Long idRegla) {
        CredPlanDescuento regla = credPlanDescuentoRepository.findById(idRegla)
                .orElseThrow(() -> new ResourceNotFoundException("Regla no encontrada"));

        if (!regla.getPlan().getIdPlan().equals(idPlan)) {
            throw new IllegalArgumentException("La regla no pertenece al plan especificado");
        }

        credPlanDescuentoRepository.delete(regla);
    }

    @Transactional
    public DescuentoConvenioDto actualizarRegla(Long idPlan, Long idRegla, ConvenioRequest.DescuentoRequest request) {
        CredPlanDescuento regla = credPlanDescuentoRepository.findById(idRegla)
                .orElseThrow(() -> new ResourceNotFoundException("Regla no encontrada con ID: " + idRegla));

        if (!regla.getPlan().getIdPlan().equals(idPlan)) {
            throw new IllegalArgumentException("La regla no pertenece al plan especificado");
        }

        regla.setDescuento(request.getPorcentajeDescuento());
        regla.setTipoDescuento(request.getTipoDescuento() != null ? request.getTipoDescuento() : "PORCENTAJE");

        if (request.getIdServicio() != null) {
            regla.setIdServicio(request.getIdServicio());
            regla.setTipoAtencion(null);
            regla.setNivelServicio(null);
        } else {

            if (request.getTipoAtencion() != null) {
                regla.setIdServicio(null);
                regla.setTipoAtencion(request.getTipoAtencion());
                regla.setNivelServicio(request.getNivelServicio());
            }
        }

        if (request.getEstado() != null) {
            regla.setEstado(request.getEstado());
        }

        CredPlanDescuento actualizado = credPlanDescuentoRepository.save(regla);
        return convertirDescuentoADto(actualizado);
    }

    private void guardarDescuentos(CredPlan convenio, List<ConvenioRequest.DescuentoRequest> descuentos) {
        for (ConvenioRequest.DescuentoRequest descReq : descuentos) {
            CredPlanDescuento.CredPlanDescuentoBuilder builder = CredPlanDescuento.builder()
                    .plan(convenio)
                    .idServicio(descReq.getIdServicio())
                    .descuento(descReq.getPorcentajeDescuento())
                    .tipoDescuento(descReq.getTipoDescuento() != null ? descReq.getTipoDescuento() : "PORCENTAJE")
                    .estado(descReq.getEstado() != null ? descReq.getEstado() : "1");

            if (descReq.getIdServicio() != null) {
                builder.tipoAtencion(null);
                builder.nivelServicio(null);
            } else {

                builder.tipoAtencion(descReq.getTipoAtencion() != null ? descReq.getTipoAtencion() : "TODOS");
                builder.nivelServicio(descReq.getNivelServicio() != null ? descReq.getNivelServicio() : 0);
            }

            credPlanDescuentoRepository.save(builder.build());
        }
    }

    private ConvenioDto convertirADto(CredPlan entity) {

        List<DescuentoConvenioDto> descuentos = credPlanDescuentoRepository
                .findActivosByPlan(entity.getIdPlan())
                .stream()
                .map(this::convertirDescuentoADto)
                .collect(Collectors.toList());

        String nombreEmpresa = "SIN EMPRESA";

        if (entity.getIdEmpresa() != null) {
            nombreEmpresa = empresaRepository.findById(entity.getIdEmpresa())
                    .map(com.pe.finanzas.empresa.entity.Empresa::getNombre)
                    .orElse("EMPRESA NO ENCONTRADA");
        } else if (entity.getIdCompania() != null) {

            nombreEmpresa = companiaRepository.findById(entity.getIdCompania())
                    .map(Compania::getNombre)
                    .orElse("DESCONOCIDO");
        }

        return ConvenioDto.builder()
                .idPlan(entity.getIdPlan())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .idCompania(entity.getIdCompania())
                .nombreEmpresa(nombreEmpresa)
                .idEmpresa(entity.getIdEmpresa())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .estado(entity.getEstado())
                .activo(entity.isActivo())
                .vigente(entity.isVigenteHoy())
                .poliza(entity.getPoliza())
                .controlPreex(entity.getControlPreex())
                .fechaCre(entity.getFechaCre())
                .idPersonalUser(entity.getIdPersonalUser())
                .fechaIni(entity.getFechaIni())
                .abrev(entity.getAbrev())
                .numCuotas(entity.getNumCuotas())
                .contador(entity.getContador())
                .detalle(entity.getDetalle())
                .correlativo(entity.getCorrelativo())
                .np(entity.getNp())
                .independiente(entity.getIndependiente())
                .paquete(entity.getPaquete())
                .planNuevo(entity.getPlanNuevo())
                .tipoFact(entity.getTipoFact())
                .contador2(entity.getContador2())
                .mostrarEmerg(entity.getMostrarEmerg())
                .segViaje(entity.getSegViaje())
                .chequeo(entity.getChequeo())
                .ggss(entity.getGgss())
                .codpro(entity.getCodpro())
                // Campos de seguro
                .copago(entity.getCopago())
                .deducible(entity.getDeducible())
                .porcentajeCoaseguro(entity.getPorcentajeCoaseguro())
                .topeAnual(entity.getTopeAnual())
                .tieneDeducible(entity.getTieneDeducible())
                .descuentos(descuentos)
                .build();
    }

    private DescuentoConvenioDto convertirDescuentoADto(CredPlanDescuento entity) {
        return DescuentoConvenioDto.builder()
                .idPlanDescuento(entity.getIdPlanDescuento())
                .idServicio(entity.getIdServicio())
                .tipoAtencion(entity.getTipoAtencion())
                .nivelServicio(entity.getNivelServicio())
                .porcentajeDescuento(entity.getDescuento())
                .tipoDescuento(entity.getTipoDescuento())
                .estado(entity.getEstado() != null ? entity.getEstado() : "1")
                .build();
    }

    @Transactional
    public ConvenioDto crearPlanParaEmpresa(Long idCompania, Long idEmpresa) {

        companiaRepository.findById(idCompania)
                .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada"));

        com.pe.finanzas.empresa.entity.Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        if (empresa.getEstado() == null || !empresa.getEstado().equals(1)) {
            empresa.setEstado(1);
            empresaRepository.save(empresa);
        }

        String codigoPlan = generarCodigo(idCompania);

        String nombrePlan = "PLAN " + codigoPlan + " - "
                + (empresa.getAbrev() != null ? empresa.getAbrev() : "EMPRESA " + idEmpresa);

        CredPlan nuevoPlan = CredPlan.builder()
                .nombre(nombrePlan)
                .codigo(codigoPlan)
                .idCompania(idCompania)
                .idEmpresa(idEmpresa)
                .fechaInicio(LocalDate.now())
                .estado("1")
                .build();

        CredPlan guardado = credPlanRepository.save(nuevoPlan);
        heredarDescuentoMasivo(guardado);
        return convertirADto(guardado);
    }

    @Transactional
    public List<ConvenioDto> vincularEmpresasMasivo(BulkVinculationRequest request) {
        log.info("Vinculando {} empresas masivamente a compañía {}", request.getIdEmpresas().size(),
                request.getIdCompania());
        log.info("Datos de seguro recibidos: Copago={}, Deducible={}, Coaseguro={}",
                request.getCopago(), request.getDeducible(), request.getPorcentajeCoaseguro());

        return request.getIdEmpresas().stream()
                .map(idEmpresa -> crearPlanParaEmpresa(request.getIdCompania(), idEmpresa,
                        request.getFechaInicio(), request.getFechaFin(), request.getEstado(), request.getNombreBase(),
                        request.getCopago(), request.getDeducible(), request.getPorcentajeCoaseguro()))
                .collect(Collectors.toList());
    }

    @Transactional
    public ConvenioDto crearPlanParaEmpresa(Long idCompania, Long idEmpresa, LocalDate fechaInicio, LocalDate fechaFin,
            String estado, String nombreBase, java.math.BigDecimal copago, java.math.BigDecimal deducible,
            java.math.BigDecimal porcentajeCoaseguro) {
        companiaRepository.findById(idCompania)
                .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada"));

        com.pe.finanzas.empresa.entity.Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        if (empresa.getEstado() == null || !empresa.getEstado().equals(1)) {
            empresa.setEstado(1);
            empresaRepository.save(empresa);
        }

        String codigoPlan = generarCodigo(idCompania);

        String prefijo = (nombreBase != null && !nombreBase.trim().isEmpty()) ? nombreBase.trim().toUpperCase()
                : "PLAN";
        String nombrePlan = prefijo + " " + codigoPlan + " - "
                + (empresa.getAbrev() != null ? empresa.getAbrev() : "EMPRESA " + idEmpresa);

        CredPlan nuevoPlan = CredPlan.builder()
                .nombre(nombrePlan)
                .codigo(codigoPlan)
                .idCompania(idCompania)
                .idEmpresa(idEmpresa)
                .fechaInicio(fechaInicio != null ? fechaInicio : LocalDate.now())
                .fechaFin(fechaFin)
                .estado(estado != null ? estado : "1")
                // Campos de seguro
                .copago(copago)
                .deducible(deducible)
                .porcentajeCoaseguro(porcentajeCoaseguro)
                .tieneDeducible((deducible != null && deducible.compareTo(java.math.BigDecimal.ZERO) > 0) ? "1" : "0")
                .build();

        log.info("Entidad CredPlan antes de guardar: {}", nuevoPlan);
        CredPlan guardado = credPlanRepository.save(nuevoPlan);
        log.info("Entidad CredPlan después de guardar: {}", guardado);

        heredarDescuentoMasivo(guardado);
        return convertirADto(guardado);
    }

    @Transactional
    public void clonarDescuentos(Long idOrigen, Long idDestino) {
        log.info("Clonando descuentos de plan {} a plan {}", idOrigen, idDestino);

        CredPlan planDestino = credPlanRepository.findById(idDestino)
                .orElseThrow(() -> new ResourceNotFoundException("Plan destino no encontrado"));

        credPlanDescuentoRepository.deleteByPlanIdPlan(idDestino);
        credPlanDescuentoRepository.flush();

        List<CredPlanDescuento> descuentosOrigen = credPlanDescuentoRepository.findActivosByPlan(idOrigen);

        for (CredPlanDescuento origen : descuentosOrigen) {
            CredPlanDescuento clon = CredPlanDescuento.builder()
                    .plan(planDestino)
                    .idServicio(origen.getIdServicio())
                    .tipoAtencion(origen.getTipoAtencion())
                    .nivelServicio(origen.getNivelServicio())
                    .factor(origen.getFactor())
                    .tipoDescuento(origen.getTipoDescuento())
                    .descuento(origen.getDescuento())
                    .codigoPlan(origen.getCodigoPlan())
                    .descuentoFijo(origen.getDescuentoFijo())
                    .descuentoPorcentaje(origen.getDescuentoPorcentaje())
                    .estado(origen.getEstado())
                    .build();

            credPlanDescuentoRepository.save(clon);
        }

        log.info("Se clonaron {} descuentos exitosamente", descuentosOrigen.size());
    }

    @Transactional(readOnly = true)
    public String generarCodigo(Long idCompania) {
        Compania compania = companiaRepository.findById(idCompania)
                .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada"));

        String parentCode = compania.getCodPlan();
        if (parentCode == null || parentCode.trim().isEmpty()) {
            parentCode = "CMP" + idCompania;
        } else {
            parentCode = parentCode.trim().toUpperCase();
        }

        CredPlan ultimoPlan = credPlanRepository.findTopByIdCompaniaOrderByIdPlanDesc(idCompania).orElse(null);

        long nextSequence = 1;
        if (ultimoPlan != null && ultimoPlan.getCodigo() != null && ultimoPlan.getCodigo().startsWith(parentCode)) {
            try {
                String currentCode = ultimoPlan.getCodigo();
                String suffix = currentCode.substring(parentCode.length());

                if (suffix.startsWith("-") || suffix.startsWith(" ")) {
                    suffix = suffix.substring(1);
                }

                if (!suffix.isEmpty()) {
                    nextSequence = Long.parseLong(suffix) + 1;
                } else {
                    nextSequence = 1;
                }
            } catch (NumberFormatException e) {
                long count = credPlanRepository.countByIdCompania(idCompania);
                nextSequence = count + 1;
            }
        } else {
            long count = credPlanRepository.countByIdCompania(idCompania);
            nextSequence = count + 1;
        }
        return String.format("%s-%03d", parentCode, nextSequence);
    }

    private void heredarDescuentoMasivo(CredPlan nuevoPlan) {
        if (nuevoPlan.getIdCompania() == null)
            return;

        log.info("Buscando descuento masivo heredable para compañía {}", nuevoPlan.getIdCompania());

        List<CredPlan> planesHermanos = credPlanRepository.findByCompania(nuevoPlan.getIdCompania());

        for (CredPlan hermano : planesHermanos) {
            if (hermano.getIdPlan().equals(nuevoPlan.getIdPlan()))
                continue;
            java.util.Optional<CredPlanDescuento> reglaGlobal = hermano.getDescuentos().stream()
                    .filter(d -> "1".equals(d.getEstado()))
                    .filter(d -> "TODOS".equalsIgnoreCase(d.getTipoAtencion()))
                    .filter(d -> d.getIdServicio() == null
                            && (d.getNivelServicio() == null || d.getNivelServicio() == 0))
                    .findFirst();

            if (reglaGlobal.isPresent()) {
                CredPlanDescuento original = reglaGlobal.get();
                log.info("Heredando descuento masivo de plan {}: Fijo={}, %= {}",
                        hermano.getIdPlan(), original.getDescuentoFijo(), original.getDescuentoPorcentaje());

                CredPlanDescuento herencia = CredPlanDescuento.builder()
                        .plan(nuevoPlan)
                        .codigoPlan(nuevoPlan.getCodigo())
                        .tipoAtencion("TODOS")
                        .nivelServicio(0)
                        .tipoDescuento(original.getTipoDescuento())
                        .descuento(original.getDescuento())
                        .descuentoFijo(original.getDescuentoFijo())
                        .descuentoPorcentaje(original.getDescuentoPorcentaje())
                        .factor(original.getFactor())
                        .estado("1")
                        .build();

                credPlanDescuentoRepository.save(herencia);
                break;
            }
        }
    }
}
