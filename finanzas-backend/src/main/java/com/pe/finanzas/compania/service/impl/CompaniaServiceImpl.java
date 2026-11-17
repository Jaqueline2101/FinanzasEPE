package com.pe.finanzas.compania.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.compania.dto.BusinessSuggestionDTO;
import com.pe.finanzas.compania.dto.CompaniaRegistroDTO;
import com.pe.finanzas.compania.dto.VinculoSeguroDTO;
import com.pe.finanzas.compania.entity.Compania;
import com.pe.finanzas.compania.entity.CompaniaPersonaVinculo;
import com.pe.finanzas.compania.repository.CompaniaPersonaVinculoRepository;
import com.pe.finanzas.compania.repository.CompaniaRepository;
import com.pe.finanzas.empresa.entity.Empresa;
import com.pe.finanzas.empresa.repository.EmpresaRepository;
import com.pe.finanzas.compania.service.CompaniaService;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import com.pe.finanzas.reports.audit.service.AuditService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompaniaServiceImpl implements CompaniaService {

        private final CompaniaRepository companiaRepository;
        private final CompaniaPersonaVinculoRepository vinculoRepository;
        private final DatosPersonalesRepository datosPersonalesRepository;
        private final EmpresaRepository empresaRepository;
        private final AuditService auditService;

        @Override
        @Transactional
        public Compania registrarCompania(CompaniaRegistroDTO dto) {

                String rucToUse = dto.getRuc();
                if ((rucToUse == null || rucToUse.isEmpty()) && dto.getCodigoIafa() != null) {
                        rucToUse = dto.getCodigoIafa();
                }

                String baseLogin = dto.getUsername();
                if (baseLogin == null || baseLogin.isEmpty()) {
                        baseLogin = rucToUse;
                }
                if (baseLogin == null || baseLogin.isEmpty()) {
                        baseLogin = "user_" + System.currentTimeMillis();
                }

                DatosPersonales savedPersona;
                java.util.Optional<DatosPersonales> existingPersona = datosPersonalesRepository.findByRuc(rucToUse);

                if (existingPersona.isPresent()) {
                        DatosPersonales persona = existingPersona.get();
                        persona.setNombre(dto.getNombre());
                        persona.setDirecc(dto.getDirecc());
                        persona.setFonLocal(dto.getFonLocal());
                        persona.setTipodoc(dto.getTipodoc());
                        persona.setNumdoc(dto.getNumdoc());
                        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
                                persona.setEmail(dto.getEmail());
                        }
                        savedPersona = datosPersonalesRepository.save(persona);
                } else {
                        String uniqueLogin = baseLogin;
                        int counter = 1;
                        while (datosPersonalesRepository.existsByLogin(uniqueLogin)) {
                                uniqueLogin = baseLogin + "_" + counter++;
                        }

                        DatosPersonales persona = DatosPersonales.builder()
                                        .active(true)
                                        .login(uniqueLogin)
                                        .nombre(dto.getNombre())
                                        .direcc(dto.getDirecc())
                                        .fonLocal(dto.getFonLocal())
                                        .ruc(rucToUse)
                                        .tipodoc(dto.getTipodoc())
                                        .numdoc(dto.getNumdoc())
                                        .apepat("")
                                        .apemat("")
                                        .sexo("J")
                                        .email(dto.getEmail())
                                        .passwd("$2a$10$X/hX9.X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X")
                                        .build();
                        savedPersona = datosPersonalesRepository.save(persona);
                }

                Compania compania = companiaRepository.findById(savedPersona.getId())
                                .orElse(Compania.builder().personaBase(savedPersona).build());

                compania.setCodigo(dto.getCodigo());
                compania.setCodigoIafa(dto.getCodigoIafa());
                compania.setCodGales(dto.getCodGales());
                compania.setCodEps(dto.getCodEps());
                compania.setCodPlan(dto.getCodPlan());
                compania.setCodCtr(dto.getCodCtr());
                compania.setCodSec(dto.getCodSec());
                compania.setIdTipoCia(dto.getIdTipoCia());
                compania.setRepresentante(dto.getRepresentante());
                compania.setAuditor(dto.getAuditor());
                compania.setLiquidador(dto.getLiquidador());
                compania.setFinancieroNombre(dto.getFinancieroNombre());
                compania.setIdPersonalUser(dto.getIdPersonalUser());
                compania.setReqAmb(dto.getReqAmb());
                compania.setReqHos(dto.getReqHos());
                compania.setReqEme(dto.getReqEme());
                compania.setReqAcc(dto.getReqAcc());
                compania.setDaysWaiting(dto.getDaysWaiting());
                compania.setCtaCte(dto.getCtaCte());
                compania.setPluctc(dto.getPluctc());
                compania.setDiasPlazo(dto.getDiasPlazo());
                compania.setTipoTarif(dto.getTipoTarif());
                compania.setTrabajaCpm(dto.getTrabajaCpm());
                compania.setImporteCpm(dto.getImporteCpm());
                compania.setAfectaRecargoEspecial(dto.getAfectaRecargoEspecial());
                compania.setEstado(dto.getEstado() != null ? dto.getEstado() : 1);
                compania.setNombre(dto.getNombre());
                compania.setNomCorto(dto.getNomCorto());
                compania.setTipoPac(dto.getTipoPac());
                compania.setChana(dto.getChana());
                compania.setManejaCob(dto.getManejaCob());
                compania.setTiptra(dto.getTiptra());
                compania.setBorrar(dto.getBorrar());
                compania.setMostrar(dto.getMostrar());
                compania.setMostrarDirec(dto.getMostrarDirec());
                compania.setMostrarDiasPlazo(dto.getMostrarDiasPlazo());
                compania.setRazSol(dto.getRazSol());
                compania.setFechaCreacion(dto.getFechaCreacion());
                compania.setFacturar(dto.getFacturar());
                compania.setContador(dto.getContador());
                compania.setClasifRepCobranzas(dto.getClasifRepCobranzas());

                Compania saved = companiaRepository.save(compania);

                auditService.logUserAction("COMPANIA",
                                companiaRepository.existsById(saved.getId()) ? "UPDATE" : "INSERT",
                                "Registró/Actualizó compañía: " + saved.getNombre() + " (RUC: "
                                                + saved.getPersonaBase().getRuc() + ")");

                return saved;
        }

        @Override
        @Transactional
        public CompaniaPersonaVinculo vincularPacienteASeguro(Long idCompania, Long idPaciente, VinculoSeguroDTO dto) {

                Compania compania = companiaRepository.findById(idCompania)
                                .orElseThrow(() -> new RuntimeException(
                                                "Compania no encontrada con ID: " + idCompania));
                DatosPersonales paciente = datosPersonalesRepository.findById(idPaciente)
                                .orElseThrow(() -> new RuntimeException(
                                                "Paciente no encontrado con IdPersonal: " + idPaciente));

                CompaniaPersonaVinculo vinculo = CompaniaPersonaVinculo.builder()
                                .compania(compania)
                                .persona(paciente)
                                .nroPoliza(dto.getNroPoliza())
                                .tipoAfiliacion(dto.getTipoAfiliacion())
                                .parentesco(dto.getParentesco())
                                .fechaInicio(dto.getFechaInicio())
                                .fechaVencimiento(dto.getFechaVencimiento())
                                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                                .build();

                CompaniaPersonaVinculo saved = vinculoRepository.save(vinculo);

                auditService.logUserAction("COMPANIA", "VINCULO",
                                "Vinculó paciente: " + paciente.getNombre() + " a seguro: " + compania.getNombre());

                return saved;
        }

        @Override
        public List<CompaniaPersonaVinculo> listarVinculos(Long idCompania) {
                return vinculoRepository.findByCompaniaId(idCompania);
        }

        @Override
        public Compania obtenerCompaniaPorId(Long id) {
                return companiaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Compañía no encontrada"));
        }

        @Override
        @Transactional
        public Compania actualizarCompania(Long id, CompaniaRegistroDTO dto) {
                Compania compania = companiaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Compania no encontrada con ID: " + id));

                String nombreAnterior = compania.getNombre();

                DatosPersonales persona = compania.getPersonaBase();
                if (persona != null) {
                        persona.setNombre(dto.getNombre());
                        persona.setLogin(dto.getUsername());
                        persona.setEmail(dto.getEmail());
                        persona.setDirecc(dto.getDirecc());
                        persona.setFonLocal(dto.getFonLocal());
                        persona.setRuc(dto.getRuc());
                        persona.setTipodoc(dto.getTipodoc());
                        persona.setNumdoc(dto.getNumdoc());

                        datosPersonalesRepository.save(persona);
                }

                compania.setCodigo(dto.getCodigo());
                compania.setCodigoIafa(dto.getCodigoIafa());
                compania.setCodGales(dto.getCodGales());
                compania.setCodEps(dto.getCodEps());
                compania.setCodPlan(dto.getCodPlan());
                compania.setCodCtr(dto.getCodCtr());
                compania.setCodSec(dto.getCodSec());
                compania.setIdTipoCia(dto.getIdTipoCia());
                compania.setRepresentante(dto.getRepresentante());
                compania.setAuditor(dto.getAuditor());
                compania.setLiquidador(dto.getLiquidador());
                compania.setFinancieroNombre(dto.getFinancieroNombre());
                compania.setIdPersonalUser(dto.getIdPersonalUser());
                compania.setReqAmb(dto.getReqAmb());
                compania.setReqHos(dto.getReqHos());
                compania.setReqEme(dto.getReqEme());
                compania.setReqAcc(dto.getReqAcc());
                compania.setDaysWaiting(dto.getDaysWaiting());
                compania.setCtaCte(dto.getCtaCte());
                compania.setPluctc(dto.getPluctc());
                compania.setDiasPlazo(dto.getDiasPlazo());
                compania.setTipoTarif(dto.getTipoTarif());
                compania.setTrabajaCpm(dto.getTrabajaCpm());
                compania.setImporteCpm(dto.getImporteCpm());
                compania.setAfectaRecargoEspecial(dto.getAfectaRecargoEspecial());
                compania.setEstado(dto.getEstado());
                compania.setNombre(dto.getNombre());
                compania.setNomCorto(dto.getNomCorto());
                compania.setTipoPac(dto.getTipoPac());
                compania.setChana(dto.getChana());
                compania.setManejaCob(dto.getManejaCob());
                compania.setTiptra(dto.getTiptra());
                compania.setBorrar(dto.getBorrar());
                compania.setMostrar(dto.getMostrar());
                compania.setMostrarDirec(dto.getMostrarDirec());
                compania.setMostrarDiasPlazo(dto.getMostrarDiasPlazo());
                compania.setRazSol(dto.getRazSol());
                compania.setFacturar(dto.getFacturar());
                compania.setFechaCreacion(dto.getFechaCreacion());
                compania.setContador(dto.getContador());
                compania.setClasifRepCobranzas(dto.getClasifRepCobranzas());

                Compania updated = companiaRepository.save(compania);

                auditService.logUserAction("COMPANIA", "UPDATE",
                                "Actualizó compañía: " + nombreAnterior + " -> " + updated.getNombre() + " (ID: " + id
                                                + ")");

                return updated;
        }

        @Override
        @Transactional
        public void eliminarCompania(Long id) {
                Compania compania = companiaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Compania no encontrada con ID: " + id));

                // Hibernate ejecutará el @SQLDelete definido (deleted = true)
                companiaRepository.delete(compania);

                auditService.logUserAction("COMPANIA", "DELETE",
                                "Eliminó lógicamente (soft-delete) compañía con ID: " + id);
        }

        @Override
        public Object obtenerPorRuc(String ruc) {

                java.util.Optional<Compania> companiaOpt = companiaRepository.findByPersonaBaseRuc(ruc)
                                .or(() -> companiaRepository.findByRazSol(ruc));

                if (companiaOpt.isPresent()) {
                        return companiaOpt.get();
                }

                java.util.Optional<Empresa> empresaOpt = empresaRepository.findByIdPersonalUser(ruc);
                if (empresaOpt.isPresent()) {
                        Empresa emp = empresaOpt.get();

                        java.util.Map<String, Object> response = new java.util.HashMap<>();
                        java.util.Map<String, Object> personaBase = new java.util.HashMap<>();

                        String nombre = emp.getAbrev() != null && !emp.getAbrev().isEmpty() ? emp.getAbrev()
                                        : (emp.getNombre() != null ? emp.getNombre() : "");
                        String razSol = emp.getNombre() != null && !emp.getNombre().isEmpty() ? emp.getNombre()
                                        : (emp.getAbrev() != null ? emp.getAbrev() : "");

                        personaBase.put("direcc", emp.getDireccion() != null ? emp.getDireccion() : "");
                        personaBase.put("nombre", razSol);
                        personaBase.put("fonLocal", emp.getTelefono() != null ? emp.getTelefono() : "");
                        personaBase.put("ruc", ruc);

                        String email = "";
                        try {
                                if (emp.getIdPersonalUser() != null) {
                                        email = datosPersonalesRepository.findByLogin(emp.getIdPersonalUser())
                                                        .map(DatosPersonales::getEmail).orElse("");
                                }
                        } catch (Exception e) {

                        }
                        personaBase.put("email", email);

                        response.put("nombre", nombre);
                        response.put("razSol", razSol);
                        response.put("personaBase", personaBase);
                        response.put("representante", emp.getRepresentante() != null ? emp.getRepresentante() : "");
                        response.put("financieroNombre", emp.getFinanciero() != null ? emp.getFinanciero() : "");

                        return response;
                }

                java.util.Optional<DatosPersonales> personaOpt = datosPersonalesRepository.findByRuc(ruc);
                if (personaOpt.isPresent()) {
                        DatosPersonales p = personaOpt.get();
                        String rz = p.getNombre();
                        return java.util.Map.of(
                                        "nombre", p.getFullName(),
                                        "razSol", rz != null ? rz : p.getFullName(),
                                        "personaBase", p);
                }

                return null;
        }

        @Override
        public java.util.List<BusinessSuggestionDTO> buscarSugerencias(String term) {
                java.util.Map<String, BusinessSuggestionDTO> suggestionsMap = new java.util.LinkedHashMap<>();

                java.util.List<com.pe.finanzas.empresa.entity.Empresa> empresas = empresaRepository
                                .findTop10ByAbrevContainingIgnoreCaseOrIdPersonalUserStartingWithOrCodigoContainingIgnoreCase(
                                                term, term, term);

                java.util.List<Compania> companias = companiaRepository
                                .findTop10ByRazSolContainingIgnoreCaseOrPersonaBaseRucStartingWithOrNombreContainingIgnoreCase(
                                                term, term, term);

                java.util.List<com.pe.finanzas.users.entity.DatosPersonales> personas = datosPersonalesRepository
                                .findTop10ByRucStartingWithOrNumdocStartingWithOrNombreContainingIgnoreCase(term, term,
                                                term);

                companias.forEach(c -> procesarSugerencia(suggestionsMap,
                                c.getPersonaBase() != null ? c.getPersonaBase().getRuc() : c.getRazSol(),
                                mejorNombreLegalCompania(c),
                                c.getPersonaBase() != null ? c.getPersonaBase().getDirecc() : "", "COMPANIA"));

                empresas.forEach(e -> procesarSugerencia(suggestionsMap, e.getIdPersonalUser(),
                                e.getAbrev() != null && e.getAbrev().length() > 5 ? e.getAbrev() : e.getCodigo(),
                                e.getDireccion(), "EMPRESA"));

                personas.forEach(
                                p -> procesarSugerencia(suggestionsMap, p.getRuc() != null ? p.getRuc() : p.getNumdoc(),
                                                p.getFullName(), p.getDirecc(), "PERSONA"));

                return new java.util.ArrayList<>(suggestionsMap.values()).stream().limit(10).toList();
        }

        private void procesarSugerencia(java.util.Map<String, BusinessSuggestionDTO> map, String ruc, String name,
                        String direcc, String type) {
                if (ruc == null || ruc.isEmpty() || name == null || name.isEmpty())
                        return;

                BusinessSuggestionDTO existing = map.get(ruc);

                if (existing == null || isBetterSuggestion(name, existing.getRazonSocial(), type, existing.getTipo())) {
                        map.put(ruc, BusinessSuggestionDTO.builder()
                                        .ruc(ruc)
                                        .razonSocial(name)
                                        .direccion(direcc)
                                        .tipo(type)
                                        .build());
                }
        }

        private boolean isBetterSuggestion(String newName, String oldName, String newType, String oldType) {
                if (oldName == null)
                        return true;

                if (newType.equals("COMPANIA") && !oldType.equals("COMPANIA"))
                        return true;

                boolean isOldNameRuc = oldName.matches("\\d+");
                boolean isNewNameRuc = newName.matches("\\d+");
                if (isOldNameRuc && !isNewNameRuc)
                        return true;
                if (!isOldNameRuc && isNewNameRuc)
                        return false;

                String upperNew = newName.toUpperCase();
                String upperOld = oldName.toUpperCase();
                boolean isNewLegal = upperNew.contains(" S.A") || upperNew.contains(" S.A.C")
                                || upperNew.contains(" S.R.L") || upperNew.contains(" SAC")
                                || upperNew.contains(" SRL");
                boolean isOldLegal = upperOld.contains(" S.A") || upperOld.contains(" S.A.C")
                                || upperOld.contains(" S.R.L") || upperOld.contains(" SAC")
                                || upperOld.contains(" SRL");

                if (isNewLegal && !isOldLegal)
                        return true;
                if (!isNewLegal && isOldLegal)
                        return false;

                return newName.length() > oldName.length();
        }

        private String mejorNombreLegalCompania(Compania c) {
                String razSol = c.getRazSol();
                String fullName = (c.getPersonaBase() != null) ? c.getPersonaBase().getFullName() : "";
                String nombreComercial = c.getNombre();

                if (razSol != null && !razSol.matches("\\d+") && razSol.trim().length() > 3) {
                        return razSol.trim();
                }

                if (nombreComercial != null && !nombreComercial.matches("\\d+")
                                && nombreComercial.trim().length() > 3) {
                        return nombreComercial.trim();
                }

                if (fullName != null && fullName.trim().length() > 3) {
                        return fullName.trim();
                }

                return nombreComercial != null ? nombreComercial : (razSol != null ? razSol : "");
        }
}
