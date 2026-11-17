package com.pe.finanzas.grupo_familiar.service;

import com.pe.finanzas.grupo_familiar.dto.GrupoFamiliarRequest;
import com.pe.finanzas.grupo_familiar.dto.GrupoFamiliarResponse;
import com.pe.finanzas.grupo_familiar.dto.ValidacionTrabajadorResponse;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.grupo_familiar.entity.GrupoFamiliar;
import com.pe.finanzas.grupo_familiar.repository.GrupoFamiliarRepository;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrupoFamiliarServiceImpl implements GrupoFamiliarService {

    private final GrupoFamiliarRepository grupoFamiliarRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;

    @Override
    @Transactional
    public GrupoFamiliarResponse registrarFamiliar(GrupoFamiliarRequest request) {
        log.info("Registrando familiar: {} para titular: {}", request.getIdFamiliar(), request.getIdTitular());

        // Validar que el titular existe y es trabajador
        DatosPersonales titular = datosPersonalesRepository.findByNumdoc(request.getIdTitular())
                .orElseThrow(() -> new RuntimeException(
                        "Trabajador titular no encontrado con DNI: " + request.getIdTitular()));

        if (!Boolean.TRUE.equals(titular.getActive())) {
            throw new RuntimeException("El DNI ingresado no corresponde a un trabajador activo");
        }

        // Validar que el familiar existe
        // Validar que el familiar existe o crearlo si se enviaron datos
        DatosPersonales familiar = datosPersonalesRepository.findByNumdoc(request.getIdFamiliar()).orElse(null);

        if (familiar == null) {
            if (request.getNombre() != null && !request.getNombre().isEmpty()) {
                // Crear familiar como nueva persona
                familiar = DatosPersonales.builder()
                        .numdoc(request.getIdFamiliar())
                        .tipodoc("01") // DNI por defecto
                        .nombre(request.getNombre())
                        .apepat(request.getApepat())
                        .apemat(request.getApemat())
                        .nacfec(request.getNacfec())
                        .sexo(request.getSexo())
                        .active(true) // Activo pero sin login
                        .build();
                familiar = datosPersonalesRepository.save(familiar);
                log.info("Nueva persona creada para familiar: {}", familiar.getNumdoc());
            } else {
                throw new ResourceNotFoundException("Familiar no encontrado con DNI: " + request.getIdFamiliar());
            }
        }

        // Verificar que no exista ya la relación
        if (grupoFamiliarRepository.existsByIdTitularAndIdFamiliar(request.getIdTitular(), request.getIdFamiliar())) {
            throw new RuntimeException("Ya existe una relación familiar entre estos DNIs");
        }

        // Crear la relación
        GrupoFamiliar grupoFamiliar = GrupoFamiliar.builder()
                .idTitular(request.getIdTitular())
                .idFamiliar(request.getIdFamiliar())
                .parentesco(request.getParentesco())
                .observaciones(request.getObservaciones())
                .estado("1")
                .fechaRegistro(LocalDate.now())
                .build();

        GrupoFamiliar saved = grupoFamiliarRepository.save(grupoFamiliar);
        log.info("Familiar registrado exitosamente con ID: {}", saved.getId());

        return mapToResponse(saved, titular, familiar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoFamiliarResponse> obtenerFamiliaresPorTitular(String idTitular) {
        log.info("Obteniendo familiares para titular: {}", idTitular);

        List<GrupoFamiliar> familiares = grupoFamiliarRepository.findByIdTitularAndEstado(idTitular, "1");

        return familiares.stream()
                .map(gf -> {
                    DatosPersonales titular = datosPersonalesRepository.findByNumdoc(gf.getIdTitular()).orElse(null);
                    DatosPersonales familiar = datosPersonalesRepository.findByNumdoc(gf.getIdFamiliar()).orElse(null);
                    return mapToResponse(gf, titular, familiar);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ValidacionTrabajadorResponse validarTrabajador(String dni) {
        log.info("Validando trabajador con DNI: {}", dni);

        var trabajador = datosPersonalesRepository.findByNumdoc(dni);

        if (trabajador.isEmpty()) {
            return ValidacionTrabajadorResponse.builder()
                    .esValido(false)
                    .mensaje("DNI no encontrado en el sistema")
                    .build();
        }

        DatosPersonales persona = trabajador.get();

        boolean esValido = Boolean.TRUE.equals(persona.getActive());

        return ValidacionTrabajadorResponse.builder()
                .esValido(esValido)
                .nombre(esValido ? persona.getNombre() + " " + persona.getApepat() + " " + persona.getApemat() : null)
                .idEmpresa(persona.getIdEmpresa())
                .mensaje(esValido ? "Trabajador válido" : "El DNI no corresponde a un trabajador activo")
                .build();
    }

    @Override
    @Transactional
    public void desactivarFamiliar(Long id) {
        log.info("Desactivando familiar con ID: {}", id);

        GrupoFamiliar grupoFamiliar = grupoFamiliarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relación familiar no encontrada con ID: " + id));

        grupoFamiliar.setEstado("0");
        grupoFamiliar.setFechaInactivacion(LocalDate.now());

        grupoFamiliarRepository.save(grupoFamiliar);
        log.info("Familiar desactivado exitosamente");
    }

    @Override
    @Transactional(readOnly = true)
    public GrupoFamiliarResponse obtenerTitularPorFamiliar(String idFamiliar) {
        log.info("Obteniendo titular para familiar: {}", idFamiliar);

        var relacion = grupoFamiliarRepository.findByIdFamiliarAndEstado(idFamiliar, "1");

        if (relacion.isEmpty()) {
            return null;
        }

        GrupoFamiliar gf = relacion.get();
        DatosPersonales titular = datosPersonalesRepository.findByNumdoc(gf.getIdTitular()).orElse(null);
        DatosPersonales familiar = datosPersonalesRepository.findByNumdoc(gf.getIdFamiliar()).orElse(null);

        return mapToResponse(gf, titular, familiar);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esFamiliarDeTrabajador(String dniFamiliar) {
        log.info("Verificando si {} es familiar de trabajador", dniFamiliar);

        var relacion = grupoFamiliarRepository.findByIdFamiliarAndEstado(dniFamiliar, "1");

        if (relacion.isEmpty()) {
            return false;
        }

        // Verificar que el titular sigue siendo trabajador activo
        DatosPersonales titular = datosPersonalesRepository.findByNumdoc(relacion.get().getIdTitular()).orElse(null);

        return titular != null && titular.getActive();
    }

    private GrupoFamiliarResponse mapToResponse(GrupoFamiliar gf, DatosPersonales titular, DatosPersonales familiar) {
        return GrupoFamiliarResponse.builder()
                .id(gf.getId())
                .idTitular(gf.getIdTitular())
                .idFamiliar(gf.getIdFamiliar())
                .parentesco(gf.getParentesco())
                .estado(gf.getEstado())
                .fechaRegistro(gf.getFechaRegistro())
                .fechaInactivacion(gf.getFechaInactivacion())
                .observaciones(gf.getObservaciones())
                .nombreTitular(titular != null ? titular.getNombre() + " " + titular.getApepat() : null)
                .nombreFamiliar(familiar != null ? familiar.getNombre() + " " + familiar.getApepat() : null)
                .build();
    }
}
