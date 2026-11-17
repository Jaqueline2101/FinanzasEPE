package com.pe.finanzas.proveedores.service.impl;

import com.pe.finanzas.proveedores.entity.Proveedor;
import com.pe.finanzas.proveedores.repository.ProveedorRepository;
import com.pe.finanzas.proveedores.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> findAll() {
        return proveedorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Proveedor> findById(Long id) {
        return proveedorRepository.findById(id);
    }

    @Override
    @Transactional
    public Proveedor save(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    @Override
    @Transactional
    public Proveedor update(Long id, Proveedor proveedor) {
        return proveedorRepository.findById(id).map(existing -> {
            existing.setRazonSocial(proveedor.getRazonSocial());
            existing.setTipoDocIdent(proveedor.getTipoDocIdent());
            existing.setNumDocIdent(proveedor.getNumDocIdent());
            existing.setDireccion(proveedor.getDireccion());
            existing.setTelefono(proveedor.getTelefono());
            existing.setEmail(proveedor.getEmail());
            existing.setDepartamento(proveedor.getDepartamento());
            existing.setProvincia(proveedor.getProvincia());
            existing.setDistrito(proveedor.getDistrito());
            existing.setEstado(proveedor.getEstado());
            return proveedorRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));

        proveedor.setEstado(0);
        proveedorRepository.save(proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Proveedor> findByRuc(String ruc) {
        return proveedorRepository.findByNumDocIdent(ruc);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkDocDuplicity(String numDocIdent, Long excludeId) {
        if (excludeId != null) {
            return proveedorRepository.existsByNumDocIdentAndIdNot(numDocIdent, excludeId);
        }
        return proveedorRepository.existsByNumDocIdent(numDocIdent);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkRazonSocialDuplicity(String razonSocial, Long excludeId) {
        if (excludeId != null) {
            return proveedorRepository.existsByRazonSocialIgnoreCaseAndIdNot(razonSocial, excludeId);
        }
        return proveedorRepository.existsByRazonSocialIgnoreCase(razonSocial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> searchByNombre(String query) {
        return proveedorRepository.findByRazonSocialContainingIgnoreCase(query);
    }
}
