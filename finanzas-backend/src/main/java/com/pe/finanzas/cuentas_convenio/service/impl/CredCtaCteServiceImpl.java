package com.pe.finanzas.cuentas_convenio.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.compania.entity.Compania;
import com.pe.finanzas.compania.repository.CompaniaRepository;
import com.pe.finanzas.cuentas_convenio.dto.CredCtaCteDto;
import com.pe.finanzas.cuentas_convenio.entity.CredCtaCte;
import com.pe.finanzas.cuentas_convenio.repository.CredCtaCteRepository;
import com.pe.finanzas.cuentas_convenio.service.CredCtaCteService;
import com.pe.finanzas.empresa.entity.Empresa;
import com.pe.finanzas.empresa.repository.EmpresaRepository;
import com.pe.finanzas.sucursales.entity.Sucursal;
import com.pe.finanzas.sucursales.repository.SucursalRepository;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredCtaCteServiceImpl implements CredCtaCteService {

    private final CredCtaCteRepository repository;
    private final DatosPersonalesRepository pacienteRepository;
    private final EmpresaRepository empresaRepository;
    private final CompaniaRepository companiaRepository;
    private final SucursalRepository sucursalRepository;
    private final com.pe.finanzas.roles.repository.RoleRepository roleRepository;

    @Override
    @Transactional
    public CredCtaCte crearCuenta(CredCtaCteDto dto) {
        log.info("Iniciando creación de cred_ctacte: {}", dto);
        try {
            DatosPersonales paciente = null;
            
            // 1. Intentar vincular por ID si viene en el DTO
            if (dto.getId() != null && dto.getId() > 0) {
                paciente = pacienteRepository.findById(dto.getId())
                        .orElseThrow(() -> new RuntimeException("Paciente no encontrado por ID"));
            } 
            // 2. Si no viene ID, intentar encontrar por DNI (Consistencia de Datos)
            else if (dto.getDni() != null && !dto.getDni().trim().isEmpty()) {
                paciente = pacienteRepository.findByNumdoc(dto.getDni()).orElse(null);
            }

            // 3. Si sigue siendo null y tenemos nombres, CREAR un registro de DatosPersonales (Shadow Patient -> Real Patient)
            if (paciente == null && dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
                log.info("Creando nuevo registro de DatosPersonales para el asegurado: {}", dto.getNombre());
                java.util.Set<com.pe.finanzas.roles.entity.Role> roles = new java.util.HashSet<>();
                roleRepository.findFirstByName("PACIENTE").ifPresent(roles::add);

                paciente = DatosPersonales.builder()
                        .nombre(dto.getNombre())
                        .apepat(dto.getApellidoPaterno() != null ? dto.getApellidoPaterno() : "")
                        .apemat(dto.getApellidoMaterno())
                        .numdoc(dto.getDni())
                        .tipodoc(dto.getTipoAsegurado() != null ? "1" : "1") // Default DNI
                        .login(dto.getDni()) // Usar DNI como login por defecto
                        .roles(roles)
                        .active(true)
                        .build();
                paciente = pacienteRepository.save(paciente);
            }

            Compania compania = companiaRepository.findById(dto.getIdCompania())
                    .orElseThrow(() -> new RuntimeException("Compañía aseguradora no encontrada"));

            Empresa empresa = null;
            if (dto.getIdEmpresa() != null) {
                empresa = empresaRepository.findById(dto.getIdEmpresa()).orElse(null);
            }

            Sucursal sucursal = null;
            if (dto.getIdSucursal() != null) {
                sucursal = sucursalRepository.findById(dto.getIdSucursal()).orElse(null);
            }

            // Generación de Nro. Cuenta Secuencial
            String uniqueCode = dto.getNroCtacte();
            if (uniqueCode == null || uniqueCode.trim().isEmpty()) {
                String companiaPrefix = compania.getCodigo() != null && !compania.getCodigo().trim().isEmpty()
                        ? compania.getCodigo()
                        : "CIA";
                Integer maxSeq = repository.findMaxSequenceByCompania(compania.getId(), companiaPrefix);
                int nextSeq = (maxSeq != null) ? maxSeq + 1 : 1;
                uniqueCode = String.format("%s-%07d", companiaPrefix, nextSeq);
            }

            CredCtaCte cuenta = CredCtaCte.builder()
                    .nroCtacte(uniqueCode)
                    .paciente(paciente)
                    .dni(dto.getDni())
                    .empresa(empresa)
                    .compania(compania)
                    .sucursal(sucursal)
                    .estado(dto.getEstado() != null ? dto.getEstado() : "ACTIVO")
                    .build();

            mapDtoToEntity(dto, cuenta);

            CredCtaCte saved = repository.save(cuenta);
            log.info("Cuenta cred_ctacte creada exitosamente con ID: {}", saved.getIdCtacte());
            return saved;
        } catch (Exception e) {
            log.error("Error al crear cuenta cred_ctacte: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CredCtaCte actualizarCuenta(Long id, CredCtaCteDto dto) {
        CredCtaCte cuenta = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta cred_ctacte no encontrada"));

        // Logic repair for Shadow Patients
        if (dto.getId() != null && dto.getId() > 0) {
            cuenta.setPaciente(pacienteRepository.findById(dto.getId()).orElse(null));
        } else if (dto.getDni() != null) {
            // Try link by DNI
            DatosPersonales p = pacienteRepository.findByNumdoc(dto.getDni()).orElse(null);
            if (p == null && dto.getNombre() != null) {
                 java.util.Set<com.pe.finanzas.roles.entity.Role> roles = new java.util.HashSet<>();
                 roleRepository.findFirstByName("PACIENTE").ifPresent(roles::add);

                 p = DatosPersonales.builder()
                        .nombre(dto.getNombre())
                        .apepat(dto.getApellidoPaterno() != null ? dto.getApellidoPaterno() : "")
                        .numdoc(dto.getDni())
                        .login(dto.getDni())
                        .roles(roles)
                        .active(true)
                        .build();
                 p = pacienteRepository.save(p);
            }
            cuenta.setPaciente(p);
        }

        if (dto.getIdEmpresa() != null) {
            cuenta.setEmpresa(empresaRepository.findById(dto.getIdEmpresa()).orElse(null));
        }

        if (dto.getIdCompania() != null) {
            cuenta.setCompania(companiaRepository.findById(dto.getIdCompania())
                    .orElseThrow(() -> new RuntimeException("Compañía no encontrada")));
        }

        if (dto.getIdSucursal() != null) {
            cuenta.setSucursal(sucursalRepository.findById(dto.getIdSucursal()).orElse(null));
        }

        mapDtoToEntity(dto, cuenta);

        return repository.save(cuenta);
    }

    private void mapDtoToEntity(CredCtaCteDto dto, CredCtaCte entity) {
        entity.setDni(dto.getDni());
        entity.setNroPoliza(dto.getNroPoliza());
        entity.setCodAsegurado(dto.getCodAsegurado());
        entity.setTipoAsegurado(dto.getTipoAsegurado());
        entity.setParentesco(dto.getParentesco());
        entity.setPlanSeguro(dto.getPlanSeguro());
        entity.setNombre(dto.getNombre());
        entity.setApellidoPaterno(dto.getApellidoPaterno());
        entity.setApellidoMaterno(dto.getApellidoMaterno());
        entity.setCodigoPlan(dto.getCodigoPlan());
        entity.setNombrePlan(dto.getNombrePlan());
        entity.setIdPlan(dto.getIdPlan());
        entity.setTipoCobertura(dto.getTipoCobertura());
        entity.setPorcentajeCobertura(dto.getPorcentajeCobertura());
        entity.setCopago(dto.getCopago());
        entity.setDeducible(dto.getDeducible());
        entity.setFechaInicio(dto.getFechaInicio());
        entity.setFechaFin(dto.getFechaFin());
        entity.setEstado(dto.getEstado());
        entity.setObservaciones(dto.getObservaciones());
        entity.setUsuarioRegistro(dto.getUsuarioRegistro());

        // Legacy 96 fields
        entity.setIdEmpFact(dto.getIdEmpFact());
        entity.setFechaIni(dto.getFechaIni());
        entity.setFechaFin(dto.getFechaFin());
        entity.setFechaCierre(dto.getFechaCierre());
        entity.setTitular(dto.getTitular());
        entity.setDocumento(dto.getDocumento());
        entity.setIdPersonalUser(dto.getIdPersonalUser());
        entity.setLimite(dto.getLimite());
        entity.setConsumo(dto.getConsumo());
        entity.setCorteLimite(dto.getCorteLimite());
        entity.setMonedaLimite(dto.getMonedaLimite());
        entity.setTipoCta(dto.getTipoCta());
        entity.setIdTipoAte(dto.getIdTipoAte());
        entity.setNumSubctas(dto.getNumSubctas());
        entity.setIdPaquete(dto.getIdPaquete());
        entity.setIdEspecialidad(dto.getIdEspecialidad());
        entity.setValesFarmacia(dto.getValesFarmacia());
        entity.setIdUserExp(dto.getIdUserExp());
        entity.setFechaExp(dto.getFechaExp());
        entity.setControl(dto.getControl());
        entity.setIdPersonalControl(dto.getIdPersonalControl());
        entity.setFechaControl(dto.getFechaControl());
        entity.setMotivoControl(dto.getMotivoControl());
        entity.setObservacion(dto.getObservacion());
        entity.setIdControl(dto.getIdControl());
        entity.setNombreFact(dto.getNombreFact());
        entity.setNroPlaca(dto.getNroPlaca());
        entity.setSiniestro(dto.getSiniestro());
        entity.setTipoFac(dto.getTipoFac());
        entity.setMotivoNoFact(dto.getMotivoNoFact());
        entity.setPunto(dto.getPunto());
        entity.setCodAuto(dto.getCodAuto());
        entity.setTopeCarta(dto.getTopeCarta());
        entity.setCobeCarta(dto.getCobeCarta());
        entity.setPrefijo(dto.getPrefijo());
        entity.setEstadoMi(dto.getEstadoMi());
        entity.setSubcta(dto.getSubcta());
        entity.setIdCtacteOrigen(dto.getIdCtacteOrigen());
        entity.setNumCarne(dto.getNumCarne());
        entity.setIdAteGrupo(dto.getIdAteGrupo());
        entity.setPaquete(dto.getPaquete());
        entity.setIdLiqUser(dto.getIdLiqUser());
        entity.setIdPersonalUpd(dto.getIdPersonalUpd());
        entity.setFechaUpd(dto.getFechaUpd());
        entity.setSemanas(dto.getSemanas());
        entity.setFechaSemana(dto.getFechaSemana());
        entity.setLatencia(dto.getLatencia());
        entity.setRevAdm(dto.getRevAdm());
        entity.setObsEmer(dto.getObsEmer());
        entity.setMensaje(dto.getMensaje());
        entity.setIdAteOdonto(dto.getIdAteOdonto());
        entity.setIdTipoAteEsp(dto.getIdTipoAteEsp());
        entity.setDepoTipoDoc(dto.getDepoTipoDoc());
        entity.setDepoRazon(dto.getDepoRazon());
        entity.setObsEstado(dto.getObsEstado());
        entity.setObsFecha(dto.getObsFecha());
        entity.setObsIdPerObs(dto.getObsIdPerObs());
        entity.setIdAteOrig(dto.getIdAteOrig());
        entity.setNroSoat(dto.getNroSoat());
        entity.setLatenciaIni(dto.getLatenciaIni());
        entity.setLatenciaFin(dto.getLatenciaFin());
        entity.setEmpTemp(dto.getEmpTemp());
        entity.setTipoFact(dto.getTipoFact());
        entity.setObsGarantia(dto.getObsGarantia());
        entity.setOtraFranq(dto.getOtraFranq());
        entity.setIdPae(dto.getIdPae());
        entity.setCarencia(dto.getCarencia());
        entity.setPreexistencia(dto.getPreexistencia());
        entity.setAuditado(dto.getAuditado());
        entity.setHonoMedic(dto.getHonoMedic());
        entity.setObsPreex(dto.getObsPreex());
        entity.setFechaEnvtrama(dto.getFechaEnvtrama());
        entity.setDedHosp(dto.getDedHosp());
        entity.setIdUpd(dto.getIdUpd());
        entity.setIdTramaConcilia(dto.getIdTramaConcilia());
        entity.setTipoAfiliacion(dto.getTipoAfiliacion());
        entity.setFechaInt(dto.getFechaInt());
        entity.setFechaAlta1(dto.getFechaAlta1());
        entity.setOrigen(dto.getOrigen());
        entity.setControlEx(dto.getControlEx());
        entity.setMixto(dto.getMixto());
        entity.setIdTitular(dto.getIdTitular());
        entity.setModEstado(dto.getModEstado());
        entity.setModIdPerUser(dto.getModIdPerUser());
        entity.setNroCarta(dto.getNroCarta());
        entity.setCrearCta(dto.getCrearCta());
        entity.setIdInterno(dto.getIdInterno());
        entity.setIntMv(dto.getIntMv());
        entity.setAlojamientoConjunto(dto.getAlojamientoConjunto());
    }

    @Override
    @Transactional
    public void eliminarCuenta(Long id) {
        repository.findById(id).ifPresent(cuenta -> {
            cuenta.setEstado("ELIMINADO");
            repository.save(cuenta);
        });
    }

    @Override
    public CredCtaCte obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta cred_ctacte no encontrada"));
    }

    @Override
    public List<CredCtaCte> listarTodas() {
        return repository.findAll().stream()
                .filter(c -> !"ELIMINADO".equalsIgnoreCase(c.getEstado()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String previsualizarNroCtacte(Long idCompania) {
        Compania compania = companiaRepository.findById(idCompania)
                .orElseThrow(() -> new RuntimeException("Compañía aseguradora no encontrada"));

        String companiaPrefix = compania.getCodigo() != null && !compania.getCodigo().trim().isEmpty()
                ? compania.getCodigo()
                : "CIA";
        Integer maxSeq = repository.findMaxSequenceByCompania(compania.getId(), companiaPrefix);
        int nextSeq = (maxSeq != null) ? maxSeq + 1 : 1;

        return String.format("%s-%07d", companiaPrefix, nextSeq);
    }

    @Override
    public List<CredCtaCte> listarPorPaciente(Long idPaciente) {
        return repository.findByPacienteId(idPaciente);
    }

    @Override
    public List<CredCtaCte> listarPorDni(String dni) {
        return repository.findByDni(dni);
    }
}
