package com.pe.finanzas.grupo_familiar.service;

import com.pe.finanzas.grupo_familiar.dto.GrupoFamiliarRequest;
import com.pe.finanzas.grupo_familiar.dto.GrupoFamiliarResponse;
import com.pe.finanzas.grupo_familiar.dto.ValidacionTrabajadorResponse;

import java.util.List;

public interface GrupoFamiliarService {

    /**
     * Registrar un nuevo familiar para un trabajador
     */
    GrupoFamiliarResponse registrarFamiliar(GrupoFamiliarRequest request);

    /**
     * Obtener todos los familiares de un trabajador titular
     */
    List<GrupoFamiliarResponse> obtenerFamiliaresPorTitular(String idTitular);

    /**
     * Validar si un DNI pertenece a un trabajador activo
     */
    ValidacionTrabajadorResponse validarTrabajador(String dni);

    /**
     * Desactivar una relación familiar
     */
    void desactivarFamiliar(Long id);

    /**
     * Obtener el trabajador titular de un familiar
     */
    GrupoFamiliarResponse obtenerTitularPorFamiliar(String idFamiliar);

    /**
     * Verificar si un paciente es familiar de un trabajador activo
     */
    boolean esFamiliarDeTrabajador(String dniFamiliar);
}
