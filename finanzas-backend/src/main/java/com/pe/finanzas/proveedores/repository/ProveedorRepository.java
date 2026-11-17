package com.pe.finanzas.proveedores.repository;

import com.pe.finanzas.proveedores.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByNumDocIdent(String numDocIdent);

    List<Proveedor> findByRazonSocialContainingIgnoreCase(String razonSocial);

    boolean existsByNumDocIdentAndIdNot(String numDocIdent, Long id);

    boolean existsByRazonSocialIgnoreCaseAndIdNot(String razonSocial, Long id);

    boolean existsByNumDocIdent(String numDocIdent);

    boolean existsByRazonSocialIgnoreCase(String razonSocial);
}
