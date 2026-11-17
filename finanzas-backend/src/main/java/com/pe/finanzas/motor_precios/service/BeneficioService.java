package com.pe.finanzas.motor_precios.service;

import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.motor_precios.dto.BeneficioDescuentoMasivoRequest;
import com.pe.finanzas.motor_precios.dto.BeneficioDto;
import com.pe.finanzas.motor_precios.dto.BeneficioRequest;
import com.pe.finanzas.motor_precios.entity.CredPlan;
import com.pe.finanzas.motor_precios.entity.CredPlanDescuento;
import com.pe.finanzas.motor_precios.repository.CredPlanDescuentoRepository;
import com.pe.finanzas.motor_precios.repository.CredPlanRepository;
import com.pe.finanzas.niveles.entity.Nivel;
import com.pe.finanzas.niveles.repository.NivelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeneficioService {

        private final CredPlanRepository credPlanRepository;
        private final CredPlanDescuentoRepository credPlanDescuentoRepository;
        private final NivelRepository nivelRepository;

        @Transactional
        public BeneficioDto crear(BeneficioRequest request) {
                log.info("Creando beneficio especial tipo={} codigo={}", request.getTipoPlan(), request.getCodigo());

                CredPlan plan = CredPlan.builder()
                                .codigo(request.getCodigo())
                                .nombre(request.getNombre())
                                .tipoPlan(request.getTipoPlan())
                                .fechaInicio(request.getFechaInicio())
                                .fechaFin(request.getFechaFin())
                                .estado(request.getEstado() != null ? request.getEstado() : "1")
                                .build();

                CredPlan guardado = credPlanRepository.save(plan);
                log.info("Beneficio especial creado con id={}", guardado.getIdPlan());

                return convertirADto(guardado);
        }

        @Transactional
        public BeneficioDto actualizar(Long id, BeneficioRequest request) {
                CredPlan plan = credPlanRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Beneficio no encontrado con ID: " + id));

                plan.setCodigo(request.getCodigo());
                plan.setNombre(request.getNombre());
                plan.setFechaInicio(request.getFechaInicio());
                plan.setFechaFin(request.getFechaFin());
                if (request.getEstado() != null) {
                        plan.setEstado(request.getEstado());
                }

                return convertirADto(credPlanRepository.save(plan));
        }

        @Transactional(readOnly = true)
        public List<BeneficioDto> listarPorTipo(String tipoPlan) {
                return credPlanRepository.findByTipoPlanOrderByIdPlanDesc(tipoPlan.toUpperCase())
                                .stream()
                                .map(this::convertirADto)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public BeneficioDto obtener(Long id) {
                CredPlan plan = credPlanRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Beneficio no encontrado con ID: " + id));
                return convertirADto(plan);
        }

        /**
         * Aplicar descuento masivo a múltiples niveles de un plan.
         * Por cada nivel seleccionado crea/actualiza una regla de descuento.
         * Sigue el mismo patrón que CatalogoServiceImpl.crearOActualizarRegla para
         * garantizar que codigoPlan, descuentoFijo y descuentoPorcentaje se persistan.
         *
         * @param idPlan  ID del CredPlan (beneficio especial)
         * @param request Lista de idNiveles + porcentaje/monto + tipo
         * @return cantidad de reglas aplicadas
         */
        @Transactional
        public int aplicarDescuentoMasivo(Long idPlan, BeneficioDescuentoMasivoRequest request) {
                CredPlan plan = credPlanRepository.findById(idPlan)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Beneficio no encontrado con ID: " + idPlan));

                java.math.BigDecimal descuentoFijo = java.math.BigDecimal.ZERO;
                java.math.BigDecimal descuentoPorcentaje = java.math.BigDecimal.ZERO;
                java.math.BigDecimal precioFijo = null;
                java.math.BigDecimal valorDescuento = java.math.BigDecimal.ZERO;
                String tipoDescuentoFinal = "PORCENTAJE";

                if ("PRECIO_FIJO".equalsIgnoreCase(request.getTipoDescuento())
                                || "TARIFA_PLANA".equalsIgnoreCase(request.getTipoDescuento())) {
                        precioFijo = request.getPrecioFijo();
                        tipoDescuentoFinal = "PRECIO_FIJO";
                        if (precioFijo != null)
                                valorDescuento = precioFijo;
                } else if ("MONTO_FIJO".equalsIgnoreCase(request.getTipoDescuento())
                                || "FIJO".equalsIgnoreCase(request.getTipoDescuento())) {
                        descuentoFijo = request.getDescuentoFijo() != null ? request.getDescuentoFijo()
                                        : java.math.BigDecimal.ZERO;
                        tipoDescuentoFinal = "MONTO_FIJO";
                        valorDescuento = descuentoFijo;
                } else {
                        descuentoPorcentaje = request.getDescuentoPorcentaje() != null
                                        ? request.getDescuentoPorcentaje()
                                        : java.math.BigDecimal.ZERO;
                        tipoDescuentoFinal = "PORCENTAJE";
                        valorDescuento = descuentoPorcentaje;
                }

                int aplicadas = 0;
                for (Long idNivelRaiz : request.getIdNiveles()) {

                        List<Nivel> jerarquia = nivelRepository.obtenerJerarquiaCompleta(idNivelRaiz);

                        for (Nivel nivelJerarquia : jerarquia) {
                                Long idNivel = nivelJerarquia.getIdNivel();

                                credPlanDescuentoRepository.deleteIndividualesByPlanAndNivel(idPlan,
                                                idNivel.intValue());

                                CredPlanDescuento regla = credPlanDescuentoRepository
                                                .findByPlanIdPlanAndNivelServicio(idPlan, idNivel.intValue())
                                                .orElseGet(() -> {
                                                        CredPlanDescuento nueva = new CredPlanDescuento();
                                                        nueva.setPlan(plan);
                                                        nueva.setNivelServicio(idNivel.intValue());
                                                        nueva.setTipoAtencion("TODOS");
                                                        nueva.setIdServicio(null);
                                                        return nueva;
                                                });

                                regla.setCodigoPlan(plan.getCodigo());
                                regla.setDescuento(valorDescuento);
                                regla.setTipoDescuento(tipoDescuentoFinal);
                                regla.setDescuentoFijo(descuentoFijo);
                                regla.setDescuentoPorcentaje(descuentoPorcentaje);
                                regla.setPrecioFijo(precioFijo);
                                regla.setEstado("1");

                                credPlanDescuentoRepository.save(regla);
                                aplicadas++;
                        }
                }

                log.info("Descuento masivo aplicado: plan={} codigo={} niveles={} tipo={} porcentaje={} fijo={} precioFijo={}",
                                idPlan, plan.getCodigo(), aplicadas, tipoDescuentoFinal, descuentoPorcentaje,
                                descuentoFijo, precioFijo);
                return aplicadas;
        }

        @Transactional
        public void eliminar(Long id) {
                CredPlan plan = credPlanRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Beneficio no encontrado con ID: " + id));
                plan.setEstado("0");
                credPlanRepository.save(plan);
        }

        private BeneficioDto convertirADto(CredPlan entity) {
                return BeneficioDto.builder()
                                .idPlan(entity.getIdPlan())
                                .codigo(entity.getCodigo())
                                .nombre(entity.getNombre())
                                .tipoPlan(entity.getTipoPlan())
                                .fechaInicio(entity.getFechaInicio())
                                .fechaFin(entity.getFechaFin())
                                .estado(entity.getEstado())
                                .activo(entity.isActivo())
                                .vigente(entity.isVigenteHoy())
                                .createdAt(entity.getCreatedAt())
                                .build();
        }
}
