package com.pe.finanzas.plan_cuentas_pmt.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.plan_cuentas_pmt.dto.HistorialMovimientoDTO;
import com.pe.finanzas.plan_cuentas_pmt.dto.PlanCuentasRegistroDTO;
import com.pe.finanzas.plan_cuentas_pmt.service.PlanCuentasService;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import com.pe.finanzas.empresa.entity.Empresa;
import com.pe.finanzas.empresa.repository.EmpresaRepository;
import com.pe.finanzas.cuentas_corrientes.entity.CuentaCorriente;
import com.pe.finanzas.cuentas_corrientes.repository.CuentaCorrienteRepository;
import com.pe.finanzas.reports.audit.entity.AuditLog;
import com.pe.finanzas.reports.audit.repository.AuditRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanCuentasServiceImpl implements PlanCuentasService {

        private final DatosPersonalesRepository personalRepository;
        private final EmpresaRepository empresaRepository;
        private final CuentaCorrienteRepository cuentaCorrienteRepository;
        private final AuditRepository auditRepository;

        @Override
        @Transactional
        public PlanCuentasRegistroDTO registrar(PlanCuentasRegistroDTO dto) {
                // Lógica de guardado orquestado
                // 1. Guardar en DatosPersonales (Común)
                DatosPersonales persona = personalRepository.findById(dto.getId() != null ? dto.getId() : -1L)
                                .orElse(new DatosPersonales());

                persona.setNombre(dto.getNombres());
                persona.setApepat(dto.getApellidoPaterno());
                persona.setApemat(dto.getApellidoMaterno());
                persona.setNumdoc(dto.getNumeroDocumento());
                persona.setTipodoc(mapTipoDocumento(dto.getTipoDocumento()));
                persona.setTipoPac(dto.getTipoRegistro()); // AISLAMIENTO: Etiquetar para PMT
                persona.setEmail(dto.getCorreo());
                persona.setFono1(dto.getTelefono());
                persona.setDirecc(dto.getDireccion());
                persona.setActive("ACTIVO".equalsIgnoreCase(dto.getEstado()));

                // Campos laborales básicos mapeados a datos_personales
                persona.setFechaIng(dto.getFechaInicio());
                persona.setFechaFin(dto.getFechaFin());
                persona.setCargo(dto.getCargo());
                persona.setFuncionPrincipal(dto.getFuncionPrincipal());
                persona.setArea(dto.getArea());
                persona.setTipoContrato(dto.getTipoContrato());
                persona.setModalidadTrabajo(dto.getModalidadTrabajo());
                persona.setModalidadPago(dto.getModalidadPago());
                persona.setJefeInmediato(dto.getJefeInmediato());
                persona.setCentroCosto(dto.getCentroCosto());
                persona.setMotivoCese(dto.getMotivoCese());
                persona.setEstadoVinculo(dto.getEstadoVinculo());

                // Campos económicos
                persona.setMontoAcordado(dto.getMontoAcordado());
                persona.setMoneda(dto.getMoneda());
                persona.setTipoPago(dto.getTipoPago());
                persona.setFrecuenciaPago(dto.getFrecuenciaPago());
                persona.setRetencionAplica(dto.getRetencionAplica());
                persona.setPorcentajeRetencion(dto.getPorcentajeRetencion());
                persona.setCuentaContableAsociada(dto.getCuentaContableAsociada());
                persona.setObservacionesPago(dto.getObservacionesPago());

                // Campos específicos (Trabajador / Médico)
                persona.setTipoPlanilla(dto.getTipoPlanilla());
                persona.setRegimenLaboral(dto.getRegimenLaboral());
                persona.setAfpOnp(dto.getAfpOnp());
                persona.setNumeroAfp(dto.getNumeroAfp());
                persona.setJornadaLaboral(dto.getJornadaLaboral());
                persona.setSueldoBase(dto.getSueldoBase());
                persona.setBonificaciones(dto.getBonificaciones());

                persona.setEspecialidad(dto.getEspecialidad());
                persona.setNumeroColegiatura(dto.getNumeroColegiatura());
                persona.setRne(dto.getRne());
                persona.setTurno(dto.getTurno());
                persona.setTipoAtencion(dto.getTipoAtencion());

                persona.setObservaciones(dto.getObservaciones());

                persona = personalRepository.save(persona);

                // 2. Si es Proveedor/Empresa, guardar en Empresa
                if ("PROVEEDOR".equalsIgnoreCase(dto.getTipoRegistro())) {
                        Empresa empresa = empresaRepository.findByNombre(dto.getRazonSocial())
                                        .orElse(new Empresa());
                        empresa.setNombre(dto.getRazonSocial());
                        empresa.setDireccion(dto.getDireccion());
                        empresa.setTelefono(dto.getTelefono());
                        empresa.setRepresentante(dto.getContactoPrincipal());
                        
                        // Campos obligatorios requeridos por la base de datos
                        if (empresa.getEstado() == null) empresa.setEstado(1); // 1 = ACTIVO
                        if (empresa.getFechaCre() == null) empresa.setFechaCre(LocalDate.now());
                        if (empresa.getIdPersonalUser() == null) empresa.setIdPersonalUser(dto.getNumeroDocumento());
                        
                        empresa = empresaRepository.save(empresa);
                        persona.setIdEmpresa(empresa.getIdEmpresa()); // Vincular persona con empresa
                        personalRepository.save(persona);
                }

                // 3. Guardar Datos Bancarios en CuentaCorriente
                if (dto.getNumeroCuenta() != null && !dto.getNumeroCuenta().trim().isEmpty()) {
                        CuentaCorriente cuenta = cuentaCorrienteRepository.findByNumeroCuenta(dto.getNumeroCuenta())
                                        .orElse(new CuentaCorriente());

                        // Asegurar campos obligatorios (@Column(nullable = false))
                        cuenta.setNombreTitular(dto.getTitularCuenta() != null && !dto.getTitularCuenta().isEmpty()
                                        ? dto.getTitularCuenta()
                                        : persona.getFullName());
                        cuenta.setBanco(dto.getBanco() != null ? dto.getBanco() : "BCP"); // Default si viniera nulo
                        cuenta.setNumeroCuenta(dto.getNumeroCuenta());
                        cuenta.setCci(dto.getCci());
                        cuenta.setMoneda(dto.getMonedaCuenta() != null ? dto.getMonedaCuenta() : "PEN"); // Default

                        // Mapeo de estado string a char (1)
                        String estadoChar = "ACTIVO".equalsIgnoreCase(dto.getEstado()) ? "A" : "I";
                        cuenta.setEstado(estadoChar);
                        cuenta.setTipoCuenta(dto.getTipoCuenta()); // Nuevo campo tipoCuenta

                        cuentaCorrienteRepository.save(cuenta);
                }

                dto.setId(persona.getId());
                return dto;
        }

        @Override
        public PlanCuentasRegistroDTO obtenerPorId(Long id, String tipoRegistro) {
                // Implementación básica de retorno
                DatosPersonales persona = personalRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));

                return PlanCuentasRegistroDTO.builder()
                                .id(persona.getId())
                                .nombres(persona.getNombre())
                                .apellidoPaterno(persona.getApepat())
                                .apellidoMaterno(persona.getApemat())
                                .numeroDocumento(persona.getNumdoc())
                                .tipoDocumento(persona.getTipodoc())
                                .correo(persona.getEmail())
                                .telefono(persona.getFono1())
                                .direccion(persona.getDirecc())
                                .estado(Boolean.TRUE.equals(persona.getActive()) ? "ACTIVO" : "INACTIVO")
                                .fechaInicio(persona.getFechaIng())
                                .fechaFin(persona.getFechaFin())
                                .cargo(persona.getCargo())
                                .funcionPrincipal(persona.getFuncionPrincipal())
                                .area(persona.getArea())
                                .tipoContrato(persona.getTipoContrato())
                                .modalidadTrabajo(persona.getModalidadTrabajo())
                                .modalidadPago(persona.getModalidadPago())
                                .jefeInmediato(persona.getJefeInmediato())
                                .centroCosto(persona.getCentroCosto())
                                .motivoCese(persona.getMotivoCese())
                                .estadoVinculo(persona.getEstadoVinculo())
                                .montoAcordado(persona.getMontoAcordado())
                                .moneda(persona.getMoneda())
                                .tipoPago(persona.getTipoPago())
                                .frecuenciaPago(persona.getFrecuenciaPago())
                                .retencionAplica(persona.getRetencionAplica())
                                .porcentajeRetencion(persona.getPorcentajeRetencion())
                                .cuentaContableAsociada(persona.getCuentaContableAsociada())
                                .observacionesPago(persona.getObservacionesPago())
                                .tipoPlanilla(persona.getTipoPlanilla())
                                .regimenLaboral(persona.getRegimenLaboral())
                                .afpOnp(persona.getAfpOnp())
                                .numeroAfp(persona.getNumeroAfp())
                                .jornadaLaboral(persona.getJornadaLaboral())
                                .sueldoBase(persona.getSueldoBase())
                                .bonificaciones(persona.getBonificaciones())
                                .especialidad(persona.getEspecialidad())
                                .numeroColegiatura(persona.getNumeroColegiatura())
                                .rne(persona.getRne())
                                .turno(persona.getTurno())
                                .tipoAtencion(persona.getTipoAtencion())
                                .observaciones(persona.getObservaciones())
                                .build();
        }

        @Override
        public List<HistorialMovimientoDTO> obtenerHistorial(Long id) {
                // Simulación de reconstrucción de historial desde AuditLog
                List<AuditLog> logs = auditRepository.searchLogsNoActions(null, LocalDateTime.now().minusYears(5),
                                LocalDateTime.now());

                return logs.stream()
                                .filter(log -> log.getDetails() != null && log.getDetails().contains(id.toString()))
                                .map(log -> HistorialMovimientoDTO.builder()
                                                .fecha(log.getTimestamp())
                                                .usuario(log.getUsername())
                                                .accion(log.getAction())
                                                .detalle(log.getDetails())
                                                .valorAnterior(log.getOldData())
                                                .valorNuevo(log.getNewData())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void eliminar(Long id) {
                if (personalRepository.existsById(id)) {
                        personalRepository.deleteById(id);
                } else {
                        throw new RuntimeException("El registro con ID " + id + " no existe.");
                }
        }

        @Override
        public List<PlanCuentasRegistroDTO> listarTodos() {
                List<String> tiposPMT = List.of("TRABAJADOR", "MEDICO", "PROVEEDOR");
                return personalRepository.findByTipoPacIn(tiposPMT).stream()
                                .map(persona -> {
                                        PlanCuentasRegistroDTO.PlanCuentasRegistroDTOBuilder builder = PlanCuentasRegistroDTO
                                                        .builder()
                                                        .id(persona.getId())
                                                        .nombres(persona.getNombre())
                                                        .apellidoPaterno(persona.getApepat())
                                                        .apellidoMaterno(persona.getApemat())
                                                        .numeroDocumento(persona.getNumdoc())
                                                        .tipoDocumento(persona.getTipodoc())
                                                        .correo(persona.getEmail())
                                                        .telefono(persona.getFono1())
                                                        .direccion(persona.getDirecc())
                                                        .estado(Boolean.TRUE.equals(persona.getActive()) ? "ACTIVO"
                                                                        : "INACTIVO")
                                                        .fechaInicio(persona.getFechaIng())
                                                        .fechaFin(persona.getFechaFin())
                                                        .tipoRegistro(persona.getTipoPac())
                                                        .cargo(persona.getCargo())
                                                        .funcionPrincipal(persona.getFuncionPrincipal())
                                                        .area(persona.getArea())
                                                        .tipoContrato(persona.getTipoContrato())
                                                        .modalidadTrabajo(persona.getModalidadTrabajo())
                                                        .modalidadPago(persona.getModalidadPago())
                                                        .jefeInmediato(persona.getJefeInmediato())
                                                        .centroCosto(persona.getCentroCosto())
                                                        .motivoCese(persona.getMotivoCese())
                                                        .estadoVinculo(persona.getEstadoVinculo())
                                                        .montoAcordado(persona.getMontoAcordado())
                                                        .moneda(persona.getMoneda())
                                                        .tipoPago(persona.getTipoPago())
                                                        .frecuenciaPago(persona.getFrecuenciaPago())
                                                        .retencionAplica(persona.getRetencionAplica())
                                                        .porcentajeRetencion(persona.getPorcentajeRetencion())
                                                        .cuentaContableAsociada(persona.getCuentaContableAsociada())
                                                        .observacionesPago(persona.getObservacionesPago())
                                                        .tipoPlanilla(persona.getTipoPlanilla())
                                                        .regimenLaboral(persona.getRegimenLaboral())
                                                        .afpOnp(persona.getAfpOnp())
                                                        .numeroAfp(persona.getNumeroAfp())
                                                        .jornadaLaboral(persona.getJornadaLaboral())
                                                        .sueldoBase(persona.getSueldoBase())
                                                        .bonificaciones(persona.getBonificaciones())
                                                        .especialidad(persona.getEspecialidad())
                                                        .numeroColegiatura(persona.getNumeroColegiatura())
                                                        .rne(persona.getRne())
                                                        .turno(persona.getTurno())
                                                        .tipoAtencion(persona.getTipoAtencion())
                                                        .observaciones(persona.getObservaciones());

                                        // Recuperar Datos Bancarios (Búsqueda por titular o CCI si existiera relación)
                                        // Por ahora buscamos por el nombre completo para mantener compatibilidad
                                        cuentaCorrienteRepository.findByNombreTitular(persona.getFullName())
                                                        .stream().findFirst().ifPresent(cuenta -> {
                                                                builder.banco(cuenta.getBanco())
                                                                                .tipoCuenta(cuenta.getTipoCuenta())
                                                                                .numeroCuenta(cuenta.getNumeroCuenta())
                                                                                .cci(cuenta.getCci())
                                                                                .titularCuenta(cuenta
                                                                                                .getNombreTitular())
                                                                                .monedaCuenta(cuenta.getMoneda());
                                                        });

                                        // Recuperar Datos de Empresa (Proveedor)
                                        if (persona.getIdEmpresa() != null) {
                                                empresaRepository.findById(persona.getIdEmpresa()).ifPresent(emp -> {
                                                        builder.razonSocial(emp.getNombre())
                                                                        .contactoPrincipal(emp.getRepresentante());
                                                });
                                        }

                                        return builder.build();
                                })
                                .collect(Collectors.toList());
        }

        private String mapTipoDocumento(String value) {
                if (value == null || value.trim().isEmpty())
                        return "01"; // Default DNI
                if (value.length() <= 2 && value.matches("\\d+"))
                        return value; // Ya es un código numérico

                return switch (value.toUpperCase()) {
                        case "DNI" -> "01";
                        case "RUC" -> "06";
                        case "CE", "C.E." -> "04";
                        case "PASAPORTE" -> "07";
                        default -> value.length() > 2 ? value.substring(0, 2) : value;
                };
        }
}
