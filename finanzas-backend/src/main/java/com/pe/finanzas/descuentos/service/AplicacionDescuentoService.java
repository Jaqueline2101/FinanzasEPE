package com.pe.finanzas.descuentos.service;

import com.pe.finanzas.descuentos.dto.DescuentoAplicable;
import com.pe.finanzas.descuentos.entity.TipoPaciente;
import com.pe.finanzas.descuentos.repository.TipoPacienteRepository;
import com.pe.finanzas.paquetes.entity.Paquete;
import com.pe.finanzas.paquetes.repository.PaqueteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.pe.finanzas.motor_precios.repository.CredPlanRepository; // Imported correctly

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AplicacionDescuentoService {

    private final TipoPacienteRepository tipoPacienteRepository;
    private final PaqueteRepository paqueteRepository;
    private final CredPlanRepository credPlanRepository;
    private final com.pe.finanzas.descuentos.repository.TipoPacienteServicioRepository tipoPacienteServicioRepository;
    private final com.pe.finanzas.catalogo.repository.CatalogoRepository catalogoRepository;

    // Constantes de prioridad
    private static final int PRIORIDAD_PAQUETE = 4;
    private static final int PRIORIDAD_EMPRESA = 3;
    private static final int PRIORIDAD_COMPANIA = 2; // Seguro
    private static final int PRIORIDAD_TIPO_PACIENTE = 1;
    private static final int PRIORIDAD_ESPECIAL = 5;

    public List<DescuentoAplicable> obtenerDescuentosAplicables(
            Long idServicio,
            String tipoPac,
            LocalDate fecha,
            Long idSucursal,
            Long idCompania,
            Long idEmpresa,
            Long idNivel) {

        log.debug("Obteniendo descuentos: Serv={}, TipoPac={}, Cia={}, Emp={}",
                idServicio, tipoPac, idCompania, idEmpresa);

        List<DescuentoAplicable> descuentos = new ArrayList<>();

        if (idEmpresa != null) {
            obtenerDescuentoPorEmpresa(idEmpresa, idServicio, idNivel).ifPresent(descuentos::add);
        }
        if (idCompania != null) {
            obtenerDescuentoPorConvenio(idCompania, idServicio, idNivel).ifPresent(descuentos::add);
        }
        if (tipoPac != null) {
            Optional<DescuentoAplicable> descTipoPac = obtenerDescuentoPorTipoPaciente(tipoPac);
            descTipoPac.ifPresent(descuentos::add);
        }
        descuentos.sort(Comparator
                .comparing(DescuentoAplicable::getPrioridad, Comparator.reverseOrder())
                .thenComparing(DescuentoAplicable::calcularBeneficioTotal, Comparator.reverseOrder()));

        log.debug("Encontrados {} descuentos aplicables", descuentos.size());
        return descuentos;
    }

    public List<DescuentoAplicable> obtenerDescuentosAplicables(
            Long idServicio, String tipoPac, LocalDate fecha, Long idSucursal) {
        return obtenerDescuentosAplicables(idServicio, tipoPac, fecha, idSucursal, null, null, null);
    }

    public List<DescuentoAplicable> obtenerDescuentosAplicables(
            Long idServicio, String tipoPac, LocalDate fecha, Long idSucursal, Long idCompania, Long idEmpresa) {
        return obtenerDescuentosAplicables(idServicio, tipoPac, fecha, idSucursal, idCompania, idEmpresa, null);
    }

    public Optional<DescuentoAplicable> seleccionarMejorDescuento(List<DescuentoAplicable> descuentos) {
        if (descuentos == null || descuentos.isEmpty()) {
            return Optional.empty();
        }

        return descuentos.stream()
                .max(Comparator
                        .comparing(DescuentoAplicable::getPrioridad)
                        .thenComparing(DescuentoAplicable::calcularBeneficioTotal));
    }

    private Optional<DescuentoAplicable> obtenerDescuentoPorTipoPaciente(String tipoPac) {
        return tipoPacienteRepository.findByCodigo(tipoPac)
                .filter(TipoPaciente::isActivo)
                .filter(tp -> tp.tieneDescuento() || tp.tieneCobertura())
                .map(tp -> DescuentoAplicable.builder()
                        .tipoDescuento("TIPO_PACIENTE")
                        .porcentajeDescuento(tp.getDsc() != null ? tp.getDsc() : BigDecimal.ZERO)
                        .porcentajeCobertura(tp.getCob() != null ? tp.getCob() : BigDecimal.ZERO)
                        .tipoPac(tp.getCodigo())
                        .descripcion("Descuento por tipo de paciente: " + tp.getNombre())
                        .prioridad(PRIORIDAD_TIPO_PACIENTE)
                        .requiereAutorizacion(false)
                        .build());
    }

    public Optional<DescuentoAplicable> obtenerDescuentoPorPaquete(Paquete paquete) {
        if (paquete == null || !paquete.isVigenteHoy()) {
            return Optional.empty();
        }

        return Optional.of(DescuentoAplicable.builder()
                .tipoDescuento("PAQUETE")
                .porcentajeDescuento(paquete.getDescuento() != null ? paquete.getDescuento() : BigDecimal.ZERO)
                .porcentajeCobertura(BigDecimal.ZERO)
                .idPaquete(paquete.getIdPaquete())
                .descripcion("Paquete: " + paquete.getNombre())
                .prioridad(PRIORIDAD_PAQUETE)
                .requiereAutorizacion(false)
                .build());
    }

    public Optional<DescuentoAplicable> obtenerDescuentoPorEmpresa(Long idEmpresa, Long idServicio, Long idNivel) {
        if (idEmpresa == null || (idServicio == null && idNivel == null)) {
            return Optional.empty();
        }

        return credPlanRepository.findPlanActivoPorEmpresa(idEmpresa)
                .flatMap(plan -> buscarReglaEnPlan(plan, idServicio, idNivel, PRIORIDAD_EMPRESA, "Convenio Empresa"));
    }

    public Optional<DescuentoAplicable> obtenerDescuentoPorEmpresa(Long idEmpresa, Long idServicio) {
        return obtenerDescuentoPorEmpresa(idEmpresa, idServicio, null);
    }

    public Optional<DescuentoAplicable> obtenerDescuentoPorPlan(Long idPlan, Long idServicio, Long idNivel) {
        if (idPlan == null || (idServicio == null && idNivel == null)) {
            return Optional.empty();
        }

        return credPlanRepository.findById(idPlan)
                .flatMap(plan -> buscarReglaEnPlan(plan, idServicio, idNivel, PRIORIDAD_COMPANIA, "Convenio Plan"));
    }

    public Optional<DescuentoAplicable> obtenerDescuentoPorPlan(Long idPlan, Long idServicio) {
        return obtenerDescuentoPorPlan(idPlan, idServicio, null);
    }

    public Optional<DescuentoAplicable> obtenerDescuentoPorConvenio(Long idCompania, Long idServicio, Long idNivel) {
        if (idCompania == null || (idServicio == null && idNivel == null)) {
            return Optional.empty();
        }

        return credPlanRepository.findPlanActivoPorCompania(idCompania)
                .flatMap(plan -> buscarReglaEnPlan(plan, idServicio, idNivel, PRIORIDAD_COMPANIA, "Convenio Seguro"));
    }

    public Optional<DescuentoAplicable> obtenerDescuentoPorConvenio(Long idCompania, Long idServicio) {
        return obtenerDescuentoPorConvenio(idCompania, idServicio, null);
    }

    private Optional<DescuentoAplicable> buscarReglaEnPlan(
            com.pe.finanzas.motor_precios.entity.CredPlan plan,
            Long idServicio,
            Long idNivel,
            int prioridad,
            String tipoDescDescripcion) {

        if (idServicio != null) {
            Optional<DescuentoAplicable> reglaServicio = plan.getDescuentos().stream()
                    .filter(d -> d.isActivo() && d.getIdServicio() != null && d.getIdServicio().equals(idServicio))
                    .max(Comparator.comparing(d -> d.getDescuento()))
                    .map(regla -> mapToDescuentoAplicable(regla, plan.getNombre(), prioridad, tipoDescDescripcion));

            if (reglaServicio.isPresent())
                return reglaServicio;
        }

        if (idNivel != null) {
            Integer nivelInt = idNivel.intValue();
            Optional<DescuentoAplicable> reglaNivel = plan.getDescuentos().stream()
                    .filter(d -> d.isActivo() && d.getIdServicio() == null && d.getNivelServicio() != null
                            && d.getNivelServicio().equals(nivelInt))
                    .max(Comparator.comparing(d -> d.getDescuento()))
                    .map(regla -> mapToDescuentoAplicable(regla, plan.getNombre(), prioridad,
                            tipoDescDescripcion + " (Nivel)"));

            if (reglaNivel.isPresent())
                return reglaNivel;
        }

        Optional<DescuentoAplicable> reglaGlobal = plan.getDescuentos().stream()
                .filter(d -> d.isActivo() && d.getIdServicio() == null
                        && (d.getNivelServicio() == null || d.getNivelServicio() == 0))
                .filter(d -> "TODOS".equalsIgnoreCase(d.getTipoAtencion()))
                .max(Comparator.comparing(d -> d.getDescuento()))
                .map(regla -> mapToDescuentoAplicable(regla, plan.getNombre(), prioridad,
                        tipoDescDescripcion + " (Global)"));

        if (reglaGlobal.isPresent())
            return reglaGlobal;

        return plan.getDescuentos().stream()
                .filter(d -> d.isActivo() && d.getIdServicio() == null
                        && (d.getNivelServicio() == null || d.getNivelServicio() == 0))
                .filter(d -> "AMBULATORIO".equalsIgnoreCase(d.getTipoAtencion()))
                .max(Comparator.comparing(d -> d.getDescuento()))
                .map(regla -> mapToDescuentoAplicable(regla, plan.getNombre(), prioridad,
                        tipoDescDescripcion + " (Gral)"));
    }

    private DescuentoAplicable mapToDescuentoAplicable(
            com.pe.finanzas.motor_precios.entity.CredPlanDescuento regla,
            String planNombre,
            int prioridad,
            String tipoDescNombre) {

        return DescuentoAplicable.builder()
                .tipoDescuento(tipoDescNombre.toUpperCase().replace(" ", "_"))
                .porcentajeDescuento("PORCENTAJE".equalsIgnoreCase(regla.getTipoDescuento())
                        ? regla.getDescuento()
                        : BigDecimal.ZERO)
                .descuentoFijo("MONTO_FIJO".equalsIgnoreCase(regla.getTipoDescuento())
                        ? regla.getDescuento()
                        : BigDecimal.ZERO)
                .precioFijo("PRECIO_FIJO".equalsIgnoreCase(regla.getTipoDescuento())
                        ? (regla.getPrecioFijo() != null ? regla.getPrecioFijo() : regla.getDescuento())
                        : null)
                .porcentajeCobertura(BigDecimal.ZERO)
                .idPlanDescuento(regla.getIdPlanDescuento())
                .descripcion(tipoDescNombre + ": " + planNombre + (regla.getIdServicio() == null ? " (General)" : ""))
                .prioridad(prioridad)
                .requiereAutorizacion(false)
                .build();
    }

    public boolean requiereAutorizacion(DescuentoAplicable descuento) {
        if (descuento == null) {
            return false;
        }

        if ("ESPECIAL".equals(descuento.getTipoDescuento())) {
            return true;
        }

        BigDecimal limite = new BigDecimal("30.00");
        return descuento.getPorcentajeDescuento() != null &&
                descuento.getPorcentajeDescuento().compareTo(limite) > 0;
    }

    public List<com.pe.finanzas.descuentos.dto.ServicioConDescuentoDTO> obtenerServiciosConDescuentosEspeciales(
            Long idTipoPaciente) {

        log.info("Obteniendo servicios con descuentos especiales para tipo paciente: {}", idTipoPaciente);

        // 1. Obtener tipo de paciente
        TipoPaciente tipoPaciente = tipoPacienteRepository.findById(idTipoPaciente)
                .orElseThrow(() -> new RuntimeException("Tipo de paciente no encontrado: " + idTipoPaciente));

        // 2. Obtener todos los descuentos activos para este tipo de paciente
        List<com.pe.finanzas.descuentos.entity.TipoPacienteServicio> descuentos = tipoPacienteServicioRepository
                .findActivosByTipoPacienteId(idTipoPaciente);

        log.info("Encontrados {} descuentos especiales", descuentos.size());

        // 3. Mapear a DTO con información del servicio
        return descuentos.stream()
                .map(descuento -> {
                    Long idServicio = Long.parseLong(descuento.getTipoServicio());
                    com.pe.finanzas.catalogo.entity.Catalogo servicio = catalogoRepository.findById(idServicio)
                            .orElse(null);

                    if (servicio == null) {
                        log.warn("Servicio no encontrado: {}", idServicio);
                        return null;
                    }

                    BigDecimal precioBase = servicio.getPrecioConIgv();
                    BigDecimal descuentoPorcentaje = descuento.getDescuento();
                    BigDecimal descuentoMonto = precioBase
                            .multiply(descuentoPorcentaje)
                            .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                    BigDecimal precioFinal = precioBase.subtract(descuentoMonto);

                    return com.pe.finanzas.descuentos.dto.ServicioConDescuentoDTO.builder()
                            .idServicio(servicio.getId())
                            .codigoServicio(servicio.getCodigo())
                            .nombreServicio(servicio.getNombreServicio())
                            .precioBase(precioBase)
                            .descuentoPorcentaje(descuentoPorcentaje)
                            .descuentoMonto(descuentoMonto)
                            .precioFinal(precioFinal)
                            .descripcionDescuento(String.format("Descuento especial %s%%", descuentoPorcentaje))
                            .idTipoPaciente(tipoPaciente.getIdTipoPaciente())
                            .nombreTipoPaciente(tipoPaciente.getNombre())
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(java.util.stream.Collectors.toList());
    }
}
