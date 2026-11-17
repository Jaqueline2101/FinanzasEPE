package com.pe.finanzas.compania.service;

import java.util.List;

import com.pe.finanzas.compania.dto.BusinessSuggestionDTO;
import com.pe.finanzas.compania.dto.CompaniaRegistroDTO;
import com.pe.finanzas.compania.dto.VinculoSeguroDTO;
import com.pe.finanzas.compania.entity.Compania;
import com.pe.finanzas.compania.entity.CompaniaPersonaVinculo;

public interface CompaniaService {
    Compania registrarCompania(CompaniaRegistroDTO dto);

    Compania actualizarCompania(Long id, CompaniaRegistroDTO dto);

    void eliminarCompania(Long id);

    CompaniaPersonaVinculo vincularPacienteASeguro(Long idCompania, Long idPaciente, VinculoSeguroDTO dto);

    List<CompaniaPersonaVinculo> listarVinculos(Long idCompania);

    Compania obtenerCompaniaPorId(Long id);

    Object obtenerPorRuc(String ruc);

    java.util.List<BusinessSuggestionDTO> buscarSugerencias(String term);
}
