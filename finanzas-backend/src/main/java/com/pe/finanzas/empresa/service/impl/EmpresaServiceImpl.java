package com.pe.finanzas.empresa.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.empresa.dto.VinculoDTO;
import com.pe.finanzas.empresa.entity.Empresa;
import com.pe.finanzas.empresa.entity.EmpresaPersonaVinculo;
import com.pe.finanzas.empresa.repository.EmpresaPersonaVinculoRepository;
import com.pe.finanzas.empresa.repository.EmpresaRepository;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import com.pe.finanzas.empresa.service.EmpresaService;
import com.pe.finanzas.reports.audit.service.AuditService;

import com.pe.finanzas.motor_precios.repository.CredPlanRepository;
import lombok.RequiredArgsConstructor;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final EmpresaPersonaVinculoRepository vinculoRepository;
    private final CredPlanRepository credPlanRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public EmpresaPersonaVinculo crearVinculo(VinculoDTO vinculoDTO) {
        Empresa empresa = empresaRepository.findById(vinculoDTO.getIdEmpresa())
                .orElseThrow(() -> new RuntimeException(
                        "Empresa no encontrada con ID: " + vinculoDTO.getIdEmpresa()));

        DatosPersonales personal = datosPersonalesRepository.findById(vinculoDTO.getIdPersonal())
                .orElseThrow(() -> new RuntimeException("Personal no encontrado con ID Personal: "
                        + vinculoDTO.getIdPersonal()));

        EmpresaPersonaVinculo vinculo = EmpresaPersonaVinculo.builder()
                .empresa(empresa)
                .personal(personal)
                .cargo(vinculoDTO.getCargo())
                .fechaInicio(vinculoDTO.getFechaInicio() != null ? vinculoDTO.getFechaInicio()
                        : java.time.LocalDate.now())
                .estado(1)
                .build();

        EmpresaPersonaVinculo guardado = vinculoRepository.save(vinculo);

        auditService.logUserAction("EMPRESA", "INSERT_VINCULO",
                "Vinculó personal: " + personal.getNombre() + " a empresa: " + empresa.getCodigo());

        return guardado;
    }

    @Override
    @Transactional
    public Empresa create(Empresa empresa) {
        if (empresa.getFechaCre() == null) {
            empresa.setFechaCre(java.time.LocalDate.now());
        }

        if (empresa.getEstado() == null) {
            empresa.setEstado(1);
        }

        Empresa saved = empresaRepository.save(empresa);

        auditService.logUserAction("EMPRESA", "INSERT",
                "Creó empresa: " + (saved.getCodigo() != null ? saved.getCodigo()
                        : "ID " + saved.getIdEmpresa()));

        return saved;
    }

    @Override
    public List<Empresa> getAll() {
        return empresaRepository.findAll();
    }

    @Override
    public Empresa getById(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Empresa update(Long id, Empresa empresaInfo) {
        Empresa empresa = getById(id);

        empresa.setRepresentante(empresaInfo.getRepresentante());
        empresa.setAuditor(empresaInfo.getAuditor());
        empresa.setLiquidador(empresaInfo.getLiquidador());
        empresa.setFinanciero(empresaInfo.getFinanciero());
        empresa.setIdPersonalUser(empresaInfo.getIdPersonalUser());
        empresa.setFax(empresaInfo.getFax());
        empresa.setEmpresasChana(empresaInfo.getEmpresasChana());
        empresa.setDireccion(empresaInfo.getDireccion());
        empresa.setTelefono(empresaInfo.getTelefono());
        empresa.setCodigo(empresaInfo.getCodigo());
        empresa.setAbrev(empresaInfo.getAbrev());
        empresa.setNombre(empresaInfo.getNombre());
        empresa.setEstado(empresaInfo.getEstado());
        empresa.setCtacte(empresaInfo.getCtacte());

        Empresa actualizado = empresaRepository.save(empresa);

        auditService.logUserAction("EMPRESA", "UPDATE",
                "Actualizó empresa ID: " + id);

        return actualizado;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        empresa.setEstado(0);
        empresaRepository.save(empresa);

        auditService.logUserAction("EMPRESA", "DELETE",
                "Desactivó empresa ID: " + id);
    }

    @Override
    public List<EmpresaPersonaVinculo> getVinculosByEmpresa(Long idEmpresa) {
        return vinculoRepository.findByEmpresaIdEmpresa(idEmpresa);
    }

    @Override
    public boolean checkRucDuplicity(String ruc, Long excludeId) {
        return empresaRepository.findByIdPersonalUser(ruc)
                .map(empresa -> {
                    if (excludeId != null && empresa.getIdEmpresa().equals(excludeId)) {
                        return false;
                    }
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empresa> listarEmpresasPorCompania(Long idCompania) {
        List<Long> empresaIds = credPlanRepository.findEmpresaIdsByCompania(idCompania);

        if (empresaIds == null || empresaIds.isEmpty()) {
            return Collections.emptyList();
        }

        return empresaRepository.findAllById(empresaIds);
    }
}
