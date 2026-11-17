package com.pe.finanzas.sucursales.service.impl;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.exception.ValidationException;
import com.pe.finanzas.sucursales.dto.SucursalDto;
import com.pe.finanzas.sucursales.entity.Sucursal;
import com.pe.finanzas.sucursales.repository.SucursalRepository;
import com.pe.finanzas.sucursales.service.SucursalService;
import com.pe.finanzas.shared.dto.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;

    // ========================================
    // CONSTANTES
    // ========================================

    private static final int MIN_NOMBRE_LENGTH = 3;
    private static final int MAX_NOMBRE_LENGTH = 100;
    private static final int MAX_DIRECCION_LENGTH = 200;
    private static final int MAX_TELEFONO_LENGTH = 20;
    private static final int MIN_TELEFONO_LENGTH = 3;

    private static final List<Integer> ESTADOS_VALIDOS = Arrays.asList(1, 0);

    // ========================================
    // CRUD BÁSICO
    // ========================================

    @Override
    public SucursalDto crear(SucursalDto dto) {
        log.info("Creating branch: {}", dto.getNombreSucursal());

        // Validaciones
        validateNombreSucursal(dto.getNombreSucursal(), null);
        validateDireccion(dto.getDireccion());
        validateTelefono(dto.getTelefono());

        // Verificar nombre único
        if (sucursalRepository.existsByNombreSucursal(dto.getNombreSucursal().trim())) {
            throw new ValidationException("Ya existe una sucursal con el nombre: " + dto.getNombreSucursal());
        }

        Sucursal sucursal = new Sucursal();
        mapDtoToEntity(dto, sucursal);
        sucursal.setEstado(1); // Activo por defecto

        // Normalizar
        sucursal.setNombreSucursal(dto.getNombreSucursal().trim());
        if (dto.getDireccion() != null) {
            sucursal.setDireccion(dto.getDireccion().trim());
        }
        if (dto.getTelefono() != null) {
            sucursal.setTelefono(dto.getTelefono().trim());
        }
        if (dto.getCelular() != null) {
            sucursal.setCelular(dto.getCelular().trim());
        }
        if (dto.getEmail() != null) {
            sucursal.setEmail(dto.getEmail().trim().toLowerCase());
        }

        Sucursal guardada = sucursalRepository.save(sucursal);

        log.info("Branch created successfully - ID: {}, Name: {}",
                guardada.getIdSucursal(), guardada.getNombreSucursal());

        return mapEntityToDto(guardada);
    }

    @Override
    public SucursalDto actualizar(Long id, SucursalDto dto) {
        log.info("Updating branch with ID: {}", id);

        // Validar ID
        validateId(id);

        // Buscar existente
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));

        // Validaciones
        if (dto.getNombreSucursal() != null) {
            validateNombreSucursal(dto.getNombreSucursal(), id);

            // Validar nombre único solo si cambia
            if (!sucursal.getNombreSucursal().equalsIgnoreCase(dto.getNombreSucursal().trim())) {
                if (sucursalRepository.existsByNombreSucursal(dto.getNombreSucursal().trim())) {
                    throw new ValidationException("Ya existe una sucursal con el nombre: " + dto.getNombreSucursal());
                }
            }

            sucursal.setNombreSucursal(dto.getNombreSucursal().trim());
        }

        if (dto.getDireccion() != null) {
            validateDireccion(dto.getDireccion());
            sucursal.setDireccion(dto.getDireccion().trim());
        }

        if (dto.getTelefono() != null) {
            validateTelefono(dto.getTelefono());
            sucursal.setTelefono(dto.getTelefono().trim());
        }

        if (dto.getCelular() != null) {
            validateTelefono(dto.getCelular()); // Reusing telefono validation for celular digits
            sucursal.setCelular(dto.getCelular().trim());
        }

        if (dto.getEmail() != null) {
            sucursal.setEmail(dto.getEmail().trim().toLowerCase());
        }

        if (dto.getEstado() != null) {
            validateEstado(dto.getEstado());
            sucursal.setEstado(dto.getEstado());
        }

        Sucursal actualizada = sucursalRepository.save(sucursal);

        log.info("Branch updated successfully: {}", id);
        return mapEntityToDto(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        log.info("Deleting branch with ID: {}", id);

        // Validar ID
        validateId(id);

        // Buscar existente
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));

        // Verificar si tiene personal activo
        Long personalActivo = sucursalRepository.countPersonalActivoBySucursal(id);
        if (personalActivo > 0) {
            throw new ValidationException(
                    "No se puede eliminar la sucursal porque tiene " +
                            personalActivo + " personal activo asignado");
        }

        // Soft delete
        sucursal.setEstado(0);
        sucursalRepository.save(sucursal);

        log.info("Branch marked as inactive: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SucursalDto> obtenerPorId(Long id) {
        log.debug("Getting branch by ID: {}", id);

        // Validar ID
        validateId(id);

        return sucursalRepository.findById(id)
                .map(this::mapEntityToDto);
    }

    // ========================================
    // LISTADOS CON PAGINACIÓN
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SucursalDto> obtenerTodas(Pageable pageable) {
        log.info("Getting all branches with pagination - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        validatePageable(pageable);

        Page<Sucursal> page = sucursalRepository.findAll(pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SucursalDto> obtenerPorEstado(Integer estado, Pageable pageable) {
        log.info("Getting branches by state: {} with pagination", estado);

        validateEstado(estado);
        validatePageable(pageable);

        Page<Sucursal> page = sucursalRepository.findByEstado(estado, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SucursalDto> buscarPorNombre(String nombre, Pageable pageable) {
        log.info("Searching branches by name: {} with pagination", nombre);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre de búsqueda no puede estar vacío");
        }

        validatePageable(pageable);

        Page<Sucursal> page = sucursalRepository.findByNombreSucursalContainingIgnoreCase(
                nombre.trim(), pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SucursalDto> obtenerConPersonal(Pageable pageable) {
        log.info("Getting branches with staff count with pagination");

        validatePageable(pageable);

        Page<Object[]> page = sucursalRepository.findSucursalesConCantidadPersonal(pageable);

        List<SucursalDto> content = page.getContent().stream()
                .map(obj -> {
                    Sucursal sucursal = (Sucursal) obj[0];
                    Long cantidad = (Long) obj[1];
                    SucursalDto dto = mapEntityToDto(sucursal);
                    dto.setCantidadPersonal(cantidad.intValue());
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponse.<SucursalDto>builder()
                .content(content)
                .number(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // ========================================
    // LISTADOS SIN PAGINACIÓN
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerTodasAsList() {
        log.debug("Getting all branches as list");

        return sucursalRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerActivasAsList() {
        log.debug("Getting active branches as list");

        return sucursalRepository.findAllActivas().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerPorEstadoAsList(Integer estado) {
        log.debug("Getting branches by state: {} as list", estado);

        validateEstado(estado);

        return sucursalRepository.findByEstado(estado).stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerSinPersonalAsList() {
        log.debug("Getting branches without staff as list");

        return sucursalRepository.findSucursalesSinPersonal().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    // ========================================
    // BÚSQUEDAS ESPECIALIZADAS
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Optional<SucursalDto> obtenerPorNombre(String nombre) {
        log.debug("Getting branch by name: {}", nombre);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre no puede estar vacío");
        }

        return sucursalRepository.findByNombreSucursal(nombre.trim())
                .map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerConCantidadPersonal() {
        log.debug("Getting branches with staff count");

        List<Object[]> resultados = sucursalRepository.findSucursalesConCantidadPersonal();

        return resultados.stream()
                .map(obj -> {
                    Sucursal sucursal = (Sucursal) obj[0];
                    Long cantidad = (Long) obj[1];
                    SucursalDto dto = mapEntityToDto(sucursal);
                    dto.setCantidadPersonal(cantidad.intValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerSinPersonal() {
        log.debug("Getting branches without staff");

        return sucursalRepository.findSucursalesSinPersonal().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    // ========================================
    // UTILIDADES
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Long contarPorEstado(Integer estado) {
        log.debug("Counting branches by state: {}", estado);

        validateEstado(estado);

        return sucursalRepository.countByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarActivas() {
        log.debug("Counting active branches");

        return sucursalRepository.countActivas();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarTotal() {
        log.debug("Counting total branches");

        return sucursalRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        log.debug("Checking if branch exists with name: {}", nombre);

        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        return sucursalRepository.existsByNombreSucursal(nombre.trim());
    }

    // ========================================
    // OPERACIONES ESPECIALES
    // ========================================

    @Override
    public void cambiarEstado(Long id, Integer nuevoEstado) {
        log.info("Changing state for branch {} to: {}", id, nuevoEstado);

        validateId(id);
        validateEstado(nuevoEstado);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));

        sucursal.setEstado(nuevoEstado);
        sucursalRepository.save(sucursal);

        log.info("State updated for branch: {}", id);
    }

    @Override
    public void activar(Long id) {
        log.info("Activating branch: {}", id);

        validateId(id);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));

        sucursal.setEstado(1);
        sucursalRepository.save(sucursal);

        log.info("Branch activated: {}", id);
    }

    @Override
    public void inactivar(Long id) {
        log.info("Inactivating branch: {}", id);

        validateId(id);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));

        // Verificar si tiene personal activo
        Long personalActivo = sucursalRepository.countPersonalActivoBySucursal(id);
        if (personalActivo > 0) {
            throw new ValidationException(
                    "No se puede inactivar la sucursal porque tiene " +
                            personalActivo + " personal activo asignado");
        }

        sucursal.setEstado(0);
        sucursalRepository.save(sucursal);

        log.info("Branch inactivated: {}", id);
    }

    // ========================================
    // MÉTODOS DE VALIDACIÓN PRIVADOS
    // ========================================

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }
    }

    private void validateNombreSucursal(String nombre, Long idExcluir) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre de la sucursal es obligatorio");
        }

        String nombreTrim = nombre.trim();

        if (nombreTrim.length() < MIN_NOMBRE_LENGTH) {
            throw new BadRequestException("El nombre debe tener al menos " +
                    MIN_NOMBRE_LENGTH + " caracteres");
        }

        if (nombreTrim.length() > MAX_NOMBRE_LENGTH) {
            throw new BadRequestException("El nombre no puede superar los " +
                    MAX_NOMBRE_LENGTH + " caracteres");
        }

        // No puede contener solo números
        if (nombreTrim.matches("^[0-9]+$")) {
            throw new BadRequestException("El nombre no puede contener solo números");
        }
    }

    private void validateDireccion(String direccion) {
        if (direccion != null && !direccion.trim().isEmpty()) {
            if (direccion.trim().length() > MAX_DIRECCION_LENGTH) {
                throw new BadRequestException("La dirección no puede superar los " +
                        MAX_DIRECCION_LENGTH + " caracteres");
            }
        }
    }

    private void validateTelefono(String telefono) {
        if (telefono != null && !telefono.trim().isEmpty()) {
            String telefonoTrim = telefono.trim();

            if (telefonoTrim.length() < MIN_TELEFONO_LENGTH) {
                throw new BadRequestException("El teléfono debe tener al menos " +
                        MIN_TELEFONO_LENGTH + " dígitos");
            }

            if (telefonoTrim.length() > MAX_TELEFONO_LENGTH) {
                throw new BadRequestException("El teléfono no puede superar los " +
                        MAX_TELEFONO_LENGTH + " caracteres");
            }

            // Solo números, espacios, guiones y paréntesis
            if (!telefonoTrim.matches("^[0-9\\s\\-()]+$")) {
                throw new BadRequestException(
                        "El teléfono solo puede contener números, espacios, guiones y paréntesis");
            }
        }
    }

    private void validateEstado(Integer estado) {
        if (estado == null) {
            throw new BadRequestException("El estado es obligatorio");
        }

        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new BadRequestException("Estado no válido. Debe ser 1 (Activo) o 0 (Inactivo)");
        }
    }

    private void validatePageable(Pageable pageable) {
        if (pageable == null) {
            throw new BadRequestException("Los parámetros de paginación son requeridos");
        }

        if (pageable.getPageSize() > 2000) {
            throw new BadRequestException("El tamaño de página no puede ser mayor a 2000");
        }

        if (pageable.getPageSize() <= 0) {
            throw new BadRequestException("El tamaño de página debe ser mayor a 0");
        }
    }

    // ========================================
    // MÉTODOS DE MAPEO
    // ========================================

    private SucursalDto mapEntityToDto(Sucursal entity) {
        SucursalDto dto = new SucursalDto();
        dto.setIdSucursal(entity.getIdSucursal());
        dto.setNombreSucursal(entity.getNombreSucursal());
        dto.setDireccion(entity.getDireccion());
        dto.setTelefono(entity.getTelefono());
        dto.setCelular(entity.getCelular());
        dto.setEmail(entity.getEmail());
        dto.setEstado(entity.getEstado());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setFechaActualizacion(entity.getFechaActualizacion());
        return dto;
    }

    private void mapDtoToEntity(SucursalDto dto, Sucursal entity) {
        if (dto.getNombreSucursal() != null) {
            entity.setNombreSucursal(dto.getNombreSucursal());
        }
        if (dto.getDireccion() != null) {
            entity.setDireccion(dto.getDireccion());
        }
        if (dto.getTelefono() != null) {
            entity.setTelefono(dto.getTelefono());
        }
        if (dto.getCelular() != null) {
            entity.setCelular(dto.getCelular());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getEstado() != null) {
            entity.setEstado(dto.getEstado());
        }
    }

    private PageResponse<SucursalDto> mapToPageResponse(Page<Sucursal> page) {
        List<SucursalDto> content = page.getContent().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());

        return PageResponse.<SucursalDto>builder()
                .content(content)
                .number(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
