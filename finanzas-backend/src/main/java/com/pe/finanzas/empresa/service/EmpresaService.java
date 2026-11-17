package com.pe.finanzas.empresa.service;

import java.util.List;

import com.pe.finanzas.empresa.dto.VinculoDTO;
import com.pe.finanzas.empresa.entity.Empresa;
import com.pe.finanzas.empresa.entity.EmpresaPersonaVinculo;

public interface EmpresaService {
    EmpresaPersonaVinculo crearVinculo(VinculoDTO vinculoDTO);

    Empresa create(Empresa empresa);

    List<Empresa> getAll();

    Empresa getById(Long id);

    Empresa update(Long id, Empresa empresa);

    void delete(Long id);

    List<EmpresaPersonaVinculo> getVinculosByEmpresa(Long idEmpresa);

    List<Empresa> listarEmpresasPorCompania(Long idCompania);

    boolean checkRucDuplicity(String ruc, Long excludeId);
}
