package com.pe.finanzas.motor_precios.service;

import com.pe.finanzas.motor_precios.dto.CalculoPrecioDto;
import com.pe.finanzas.motor_precios.entity.CredPlan;
import com.pe.finanzas.motor_precios.entity.CredPlanDescuento;
import com.pe.finanzas.motor_precios.repository.CredPlanDescuentoRepository;
import com.pe.finanzas.motor_precios.repository.CredPlanRepository;
import com.pe.finanzas.compania.repository.CompaniaRepository;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MotorPreciosService {

        private final DatosPersonalesRepository datosPersonalesRepository;
        private final CompaniaRepository companiaRepository;
        private final CredPlanRepository credPlanRepository;
        private final CredPlanDescuentoRepository credPlanDescuentoRepository;
        private final com.pe.finanzas.catalogo.repository.CatalogoRepository catalogoRepository;

        // Constantes
        private static final BigDecimal IGV_RATE = new BigDecimal("0.18");
        private static final BigDecimal CIEN = new BigDecimal("100");
        private static final int SCALE = 4;

        /**
         * Calcula el precio final con descuento e IGV (Versión Legacy)
         */
        public CalculoPrecioDto calcularPrecio(
                        Long idPaciente,
                        BigDecimal precioBase,
                        String tipoAtencion,
                        Integer nivelServicio) {
                return calcularPrecio(idPaciente, null, precioBase, tipoAtencion, nivelServicio);
        }

        /**
         * Calcula el precio final con descuento e IGV
         * Soporta lógica "Unidad * Factor" si se proporciona idServicio
         * 
         * @param idPaciente    ID del paciente
         * @param idServicio    ID del servicio (Opcional, pero recomendado para cálculo
         *                      exacto)
         * @param precioBase    Precio base del servicio (Precio Lista)
         * @param tipoAtencion  AMBULATORIO, HOSPITALARIO, EMERGENCIA
         * @param nivelServicio Nivel de servicio (1, 2, 3...)
         * @return CalculoPrecioDto con VV, IGV y PV
         */
        public CalculoPrecioDto calcularPrecio(
                        Long idPaciente,
                        Long idServicio,
                        BigDecimal precioBase,
                        String tipoAtencion,
                        Integer nivelServicio) {

                log.debug("Calculando precio para paciente {} servicio {} tipo {} nivel {}",
                                idPaciente, idServicio, tipoAtencion, nivelServicio);

                // 1. Obtener datos del paciente (Compañía y Empresa)
                Optional<com.pe.finanzas.users.entity.DatosPersonales> pacienteOpt = datosPersonalesRepository
                                .findById(idPaciente);

                if (pacienteOpt.isEmpty()) {
                        log.debug("Paciente no encontrado, aplicando precio normal");
                        return calcularSinDescuento(precioBase);
                }

                com.pe.finanzas.users.entity.DatosPersonales paciente = pacienteOpt.get();
                Long idCompania = paciente.getIdCompania();
                Long idEmpresa = paciente.getIdEmpresa();

                if (idCompania == null && idEmpresa == null) {
                        log.debug("Paciente sin compañía ni empresa, aplicando precio normal");
                        return calcularSinDescuento(precioBase);
                }

                // 2. Buscar plan activo y vigente (Prioridad: Empresa > Compañía)
                List<CredPlan> planesVigentes = new java.util.ArrayList<>();

                if (idEmpresa != null) {
                        planesVigentes.addAll(credPlanRepository.findVigentesByEmpresa(idEmpresa, LocalDate.now()));
                }

                if (planesVigentes.isEmpty() && idCompania != null) {
                        planesVigentes.addAll(credPlanRepository.findVigentesByCompania(idCompania, LocalDate.now()));
                }

                if (planesVigentes.isEmpty()) {
                        log.debug("No hay planes vigentes para Cia={} Emp={}", idCompania, idEmpresa);
                        return calcularSinDescuento(precioBase);
                }

                // Tomar el primer plan vigente
                CredPlan plan = planesVigentes.get(0);

                // 3. Buscar regla de descuento aplicable
                // Intentar buscar regla específica por servicio primero si tenemos el ID
                Optional<CredPlanDescuento> reglaOpt = Optional.empty();

                if (idServicio != null) {
                        reglaOpt = credPlanDescuentoRepository.findByPlanAndServicio(plan.getIdPlan(), idServicio);
                }

                if (reglaOpt.isEmpty()) {
                        reglaOpt = buscarReglaAplicable(plan.getIdPlan(), tipoAtencion, nivelServicio);
                }

                if (reglaOpt.isEmpty()) {
                        log.debug("No hay regla de descuento para tipo {} nivel {}",
                                        tipoAtencion, nivelServicio);
                        return calcularSinDescuento(precioBase);
                }

                CredPlanDescuento regla = reglaOpt.get();

                // 4. Lógica "Unidad * Factor" vs Precio Base
                BigDecimal precioCalculo = precioBase;

                if (idServicio != null && regla.getFactor() != null
                                && regla.getFactor().compareTo(BigDecimal.ZERO) > 0) {
                        // Buscar servicio para obtener su unidad
                        Optional<com.pe.finanzas.catalogo.entity.Catalogo> servicioOpt = catalogoRepository
                                        .findById(idServicio);

                        if (servicioOpt.isPresent()) {
                                BigDecimal unidad = servicioOpt.get().getUnidad();
                                if (unidad != null && unidad.compareTo(BigDecimal.ZERO) > 0) {
                                        // CÁLCULO UNIDAD * FACTOR
                                        // Ejemplo: 10 und * 5.5 soles/und = 55 soles
                                        precioCalculo = unidad.multiply(regla.getFactor()).setScale(SCALE,
                                                        RoundingMode.HALF_UP);
                                        log.info("Aplicando precio por factor: Unidad {} * Factor {} = {}", unidad,
                                                        regla.getFactor(), precioCalculo);
                                }
                        }
                }

                // 5. Calcular descuento (sobre el precioCalculo, que puede ser el base o el
                // calculado por factor)
                BigDecimal montoDescuento = calcularMontoDescuento(precioCalculo, regla);

                // 6. Calcular VV, IGV y PV
                return calcularConDescuento(precioCalculo, montoDescuento, regla, plan);
        }

        /**
         * Busca la regla de descuento más específica aplicable
         */
        private Optional<CredPlanDescuento> buscarReglaAplicable(
                        Long idPlan,
                        String tipoAtencion,
                        Integer nivelServicio) {

                // Buscar reglas por tipo de atención y nivel
                List<CredPlanDescuento> reglas = credPlanDescuentoRepository
                                .findByPlanTipoAtencionAndNivel(idPlan, tipoAtencion, nivelServicio);

                if (!reglas.isEmpty()) {
                        // Priorizar reglas con nivel específico sobre reglas generales
                        return reglas.stream()
                                        .filter(r -> r.getNivelServicio() != null
                                                        && r.getNivelServicio().equals(nivelServicio))
                                        .findFirst()
                                        .or(() -> reglas.stream().findFirst());
                }

                // Si no hay regla específica, buscar regla general (TODOS)
                List<CredPlanDescuento> reglasGenerales = credPlanDescuentoRepository.findByPlanAndTipoAtencion(idPlan,
                                "TODOS");
                return reglasGenerales.stream().findFirst();
        }

        /**
         * Calcula el monto del descuento según la regla
         */
        private BigDecimal calcularMontoDescuento(BigDecimal precioBase, CredPlanDescuento regla) {
                if (regla.esPorcentaje()) {
                        // Descuento por porcentaje
                        return precioBase.multiply(regla.getDescuento())
                                        .divide(CIEN, SCALE, RoundingMode.HALF_UP);
                } else {
                        // Descuento por monto fijo
                        return regla.getDescuento();
                }
        }

        /**
         * Calcula precio con descuento
         * 
         * Fórmulas:
         * - VV = Precio Base - Descuento
         * - IGV = VV * 0.18
         * - PV = VV + IGV
         */
        private CalculoPrecioDto calcularConDescuento(
                        BigDecimal precioBase,
                        BigDecimal montoDescuento,
                        CredPlanDescuento regla,
                        CredPlan plan) {

                // Valor Venta (VV)
                BigDecimal valorVenta = precioBase.subtract(montoDescuento)
                                .setScale(SCALE, RoundingMode.HALF_UP);

                // IGV (18%)
                BigDecimal igv = valorVenta.multiply(IGV_RATE)
                                .setScale(SCALE, RoundingMode.HALF_UP);

                // Precio de Venta (PV)
                BigDecimal precioVenta = valorVenta.add(igv)
                                .setScale(SCALE, RoundingMode.HALF_UP);

                // Porcentaje de descuento
                BigDecimal porcentajeDescuento = montoDescuento.multiply(CIEN)
                                .divide(precioBase, 2, RoundingMode.HALF_UP);

                return CalculoPrecioDto.builder()
                                .precioBase(precioBase)
                                .montoDescuento(montoDescuento)
                                .porcentajeDescuento(porcentajeDescuento)
                                .valorVenta(valorVenta)
                                .igv(igv)
                                .precioVenta(precioVenta)
                                .tipoAtencion(regla.getTipoAtencion())
                                .nivelServicio(regla.getNivelServicio())
                                .descripcionDescuento(String.format("Plan: %s - %s%%",
                                                plan.getNombre(), porcentajeDescuento))
                                .idPlan(plan.getIdPlan())
                                .nombrePlan(plan.getNombre())
                                .build();
        }

        /**
         * Calcula precio sin descuento
         */
        private CalculoPrecioDto calcularSinDescuento(BigDecimal precioBase) {
                BigDecimal valorVenta = precioBase.setScale(SCALE, RoundingMode.HALF_UP);
                BigDecimal igv = valorVenta.multiply(IGV_RATE)
                                .setScale(SCALE, RoundingMode.HALF_UP);
                BigDecimal precioVenta = valorVenta.add(igv)
                                .setScale(SCALE, RoundingMode.HALF_UP);

                return CalculoPrecioDto.builder()
                                .precioBase(precioBase)
                                .montoDescuento(BigDecimal.ZERO)
                                .porcentajeDescuento(BigDecimal.ZERO)
                                .valorVenta(valorVenta)
                                .igv(igv)
                                .precioVenta(precioVenta)
                                .descripcionDescuento("Sin descuento")
                                .build();
        }

        /**
         * Recálculo inverso: De Precio de Venta (PV) a Valor Venta (VV)
         * 
         * Fórmula:
         * - VV = PV / 1.18
         * - IGV = PV - VV
         * - Descuento = Precio Base - VV
         */
        public CalculoPrecioDto recalcularDesdePrecioVenta(
                        BigDecimal precioBase,
                        BigDecimal precioVenta) {

                log.debug("Recálculo inverso: PV={} Base={}", precioVenta, precioBase);

                // VV = PV / 1.18
                BigDecimal divisor = BigDecimal.ONE.add(IGV_RATE);
                BigDecimal valorVenta = precioVenta.divide(divisor, SCALE, RoundingMode.HALF_UP);

                // IGV = PV - VV
                BigDecimal igv = precioVenta.subtract(valorVenta)
                                .setScale(SCALE, RoundingMode.HALF_UP);

                // Descuento = Precio Base - VV
                BigDecimal montoDescuento = precioBase.subtract(valorVenta)
                                .setScale(SCALE, RoundingMode.HALF_UP);

                // Porcentaje de descuento
                BigDecimal porcentajeDescuento = BigDecimal.ZERO;
                if (precioBase.compareTo(BigDecimal.ZERO) > 0) {
                        porcentajeDescuento = montoDescuento.multiply(CIEN)
                                        .divide(precioBase, 2, RoundingMode.HALF_UP);
                }

                return CalculoPrecioDto.builder()
                                .precioBase(precioBase)
                                .montoDescuento(montoDescuento)
                                .porcentajeDescuento(porcentajeDescuento)
                                .valorVenta(valorVenta)
                                .igv(igv)
                                .precioVenta(precioVenta)
                                .descripcionDescuento("Recálculo inverso")
                                .build();
        }

        /**
         * Calcula solo el IGV de un valor venta
         */
        public BigDecimal calcularIGV(BigDecimal valorVenta) {
                return valorVenta.multiply(IGV_RATE)
                                .setScale(SCALE, RoundingMode.HALF_UP);
        }

        /**
         * Calcula el precio de venta desde un valor venta
         */
        public BigDecimal calcularPrecioVentaDesdeVV(BigDecimal valorVenta) {
                BigDecimal igv = calcularIGV(valorVenta);
                return valorVenta.add(igv).setScale(SCALE, RoundingMode.HALF_UP);
        }

        /**
         * Calcula el valor venta desde un precio de venta
         */
        public BigDecimal calcularValorVentaDesdePV(BigDecimal precioVenta) {
                BigDecimal divisor = BigDecimal.ONE.add(IGV_RATE);
                return precioVenta.divide(divisor, SCALE, RoundingMode.HALF_UP);
        }
}
