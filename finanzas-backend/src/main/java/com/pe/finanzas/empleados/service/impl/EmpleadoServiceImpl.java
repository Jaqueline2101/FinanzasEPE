package com.pe.finanzas.empleados.service.impl;

import com.pe.finanzas.empleados.dto.EmpleadoDTO;
import com.pe.finanzas.empleados.service.EmpleadoService;
import com.pe.finanzas.grupo_familiar.repository.GrupoFamiliarRepository;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {

    private final DatosPersonalesRepository userRepository;
    private final GrupoFamiliarRepository grupoFamiliarRepository;
    private final com.pe.finanzas.roles.repository.RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> listarEmpleados() {
        return userRepository.findAllEmpleados().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoDTO obtenerPorId(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoDTO obtenerPorDni(String dni) {
        return userRepository.findByNumdoc(dni)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public EmpleadoDTO registrarEmpleado(EmpleadoDTO dto) {

        if (userRepository.findByNumdoc(dto.getNumdoc()).isPresent()) {
            throw new RuntimeException("El DNI ya está registrado");
        }

        DatosPersonales entity = mapToEntity(dto);
        entity.setActive(true);
        if (entity.getLogin() == null)
            entity.setLogin(dto.getNumdoc());

        // Asignar Rol por defecto para Empleados (necesario para el nuevo filtro de
        // búsqueda)
        com.pe.finanzas.roles.entity.Role defaultRole = roleRepository.findFirstByName("USUARIO")
                .orElse(null);
        if (defaultRole != null) {
            java.util.Set<com.pe.finanzas.roles.entity.Role> roles = new java.util.HashSet<>();
            roles.add(defaultRole);
            entity.setRoles(roles);
        }

        DatosPersonales saved = userRepository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public EmpleadoDTO actualizarEmpleado(Long id, EmpleadoDTO dto) {
        DatosPersonales entity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        entity.setVerNombre(dto.getNombre());
        entity.setApepat(dto.getApepat());
        entity.setApemat(dto.getApemat());
        entity.setDirecc(dto.getDirecc());
        entity.setFono1(dto.getFono1());
        entity.setNacfec(dto.getNacfec());
        entity.setSexo(dto.getSexo());
        entity.setEstciv(dto.getEstadoCivil());
        entity.setRuc(dto.getRuc());

        entity.setFechaIng(dto.getFechaIngreso());
        entity.setIdOcupac(dto.getIdOcupacion());
        entity.setNomOcupacion(dto.getNombreOcupacion());
        entity.setCentLabor(dto.getCentroLabor());
        entity.setModalidadTrabajo(dto.getModalidadTrabajo());
        entity.setTipoContrato(dto.getTipoContrato());
        entity.setFechaFin(dto.getFechaFin());
        entity.setRenovacion(dto.getRenovacion());
        entity.setSueldoBase(dto.getSueldoBase());
        if (dto.getEstado() != null)
            entity.setActive(dto.getEstado());

        DatosPersonales saved = userRepository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, Boolean estado) {
        DatosPersonales entity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        entity.setActive(estado);
        userRepository.save(entity);
    }

    private EmpleadoDTO mapToDTO(DatosPersonales entity) {
        return EmpleadoDTO.builder()
                .id(entity.getId())
                .nombre(entity.getVerNombre())
                .apepat(entity.getApepat())
                .apemat(entity.getApemat())
                .numdoc(entity.getNumdoc())
                .ruc(entity.getRuc())
                .direcc(entity.getDirecc())
                .fono1(entity.getFono1())
                .email(entity.getEmail())
                .nacfec(entity.getNacfec())
                .sexo(entity.getSexo())
                .estadoCivil(entity.getEstciv())

                .idOcupacion(entity.getIdOcupac())
                .nombreOcupacion(entity.getNomOcupacion())
                .fechaIngreso(entity.getFechaIng())
                .fechaFin(entity.getFechaFin())
                .centroLabor(entity.getCentLabor())
                .modalidadTrabajo(entity.getModalidadTrabajo())
                .tipoContrato(entity.getTipoContrato())
                .renovacion(entity.getRenovacion())
                .sueldoBase(entity.getSueldoBase())
                .estado(entity.getActive())
                .cantidadFamiliares(entity.getNumdoc() != null
                        ? (int) grupoFamiliarRepository.countByIdTitularAndEstado(entity.getNumdoc(), "1")
                        : 0)
                .roles(entity.getRoles() != null
                        ? entity.getRoles().stream()
                                .map(com.pe.finanzas.roles.entity.Role::getName)
                                .collect(java.util.stream.Collectors.toList())
                        : java.util.Collections.emptyList())
                .build();
    }

    private DatosPersonales mapToEntity(EmpleadoDTO dto) {
        return DatosPersonales.builder()
                .verNombre(dto.getNombre())
                .apepat(dto.getApepat())
                .apemat(dto.getApemat())
                .numdoc(dto.getNumdoc())
                .ruc(dto.getRuc())
                .tipodoc("01")
                .direcc(dto.getDirecc())
                .fono1(dto.getFono1())
                .email(dto.getEmail())
                .nacfec(dto.getNacfec())
                .sexo(dto.getSexo())
                .estciv(dto.getEstadoCivil())
                .fechaIng(dto.getFechaIngreso())
                .idOcupac(dto.getIdOcupacion())
                .nomOcupacion(dto.getNombreOcupacion())
                .centLabor(dto.getCentroLabor())
                .modalidadTrabajo(dto.getModalidadTrabajo())
                .tipoContrato(dto.getTipoContrato())
                .fechaFin(dto.getFechaFin())
                .renovacion(dto.getRenovacion())
                .sueldoBase(dto.getSueldoBase())
                .build();
    }
}
