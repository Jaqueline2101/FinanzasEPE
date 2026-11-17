package com.pe.finanzas.empleados.service;

import com.pe.finanzas.empleados.dto.EmpleadoDTO;
import java.util.List;

public interface EmpleadoService {
    List<EmpleadoDTO> listarEmpleados();

    EmpleadoDTO obtenerPorId(Long id);

    EmpleadoDTO obtenerPorDni(String dni);

    EmpleadoDTO registrarEmpleado(EmpleadoDTO empleadoDTO);

    EmpleadoDTO actualizarEmpleado(Long id, EmpleadoDTO empleadoDTO);

    void cambiarEstado(Long id, Boolean estado);
}
