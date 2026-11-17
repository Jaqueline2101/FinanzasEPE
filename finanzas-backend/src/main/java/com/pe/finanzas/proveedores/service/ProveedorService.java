package com.pe.finanzas.proveedores.service;

import com.pe.finanzas.proveedores.entity.Proveedor;
import java.util.List;
import java.util.Optional;

public interface ProveedorService {
    List<Proveedor> findAll();

    Optional<Proveedor> findById(Long id);

    Proveedor save(Proveedor proveedor);

    Proveedor update(Long id, Proveedor proveedor);

    void delete(Long id);

    Optional<Proveedor> findByRuc(String ruc);

    boolean checkDocDuplicity(String numDocIdent, Long excludeId);

    boolean checkRazonSocialDuplicity(String razonSocial, Long excludeId);

    List<Proveedor> searchByNombre(String query);
}
