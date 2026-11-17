package com.pe.finanzas.empleados.controller;

import com.pe.finanzas.empleados.dto.EmpleadoDTO;
import com.pe.finanzas.empleados.service.EmpleadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rrhh/empleados")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @GetMapping
    public ResponseEntity<List<EmpleadoDTO>> listar() {
        return ResponseEntity.ok(empleadoService.listarEmpleados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.obtenerPorId(id));
    }

    @GetMapping("/busqueda/{dni}")
    public ResponseEntity<EmpleadoDTO> buscarPorDni(@PathVariable String dni) {
        EmpleadoDTO empleado = empleadoService.obtenerPorDni(dni);
        if (empleado == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(empleado);
    }

    @PostMapping
    public ResponseEntity<EmpleadoDTO> crear(@RequestBody EmpleadoDTO dto) {
        return ResponseEntity.ok(empleadoService.registrarEmpleado(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> actualizar(@PathVariable Long id, @RequestBody EmpleadoDTO dto) {
        return ResponseEntity.ok(empleadoService.actualizarEmpleado(id, dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo) {
        empleadoService.cambiarEstado(id, activo);
        return ResponseEntity.ok().build();
    }
}
