package com.pe.finanzas.cuentas_convenio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pe.finanzas.cuentas_convenio.entity.CredCtaCte;

@Repository
public interface CredCtaCteRepository extends JpaRepository<CredCtaCte, Long> {

    Optional<CredCtaCte> findByNroCtacte(String nroCtacte);

    @Query("SELECT c FROM CredCtaCte c WHERE c.compania.id = :idCompania AND c.estado = 'ACTIVO'")
    List<CredCtaCte> findActivosByCompania(Long idCompania);

    @Query("SELECT c FROM CredCtaCte c WHERE c.estado = 'ACTIVO'")
    List<CredCtaCte> findAllActivos();

    List<CredCtaCte> findByDni(String dni);

    // Búsqueda por el nuevo campo id (vínculo personal)
    List<CredCtaCte> findByPacienteId(Long idPersonal);

    // Búsqueda del máximo correlativo para una compañía
    @Query(value = "SELECT MAX(CAST(SUBSTRING(nro_ctacte, LENGTH(:prefix) + 2) AS INTEGER)) " +
            "FROM cred_ctacte " +
            "WHERE id_compania = :idCompania " +
            "AND nro_ctacte ~ CONCAT('^', :prefix, '-[0-9]+$')", nativeQuery = true)
    Integer findMaxSequenceByCompania(Long idCompania, String prefix);
}
