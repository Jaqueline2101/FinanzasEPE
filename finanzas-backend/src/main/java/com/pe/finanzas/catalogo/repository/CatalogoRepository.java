package com.pe.finanzas.catalogo.repository;

import com.pe.finanzas.catalogo.entity.Catalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogoRepository extends JpaRepository<Catalogo, Long> {

        Optional<Catalogo> findByCodigo(String codigo);

        Optional<Catalogo> findByCodigoBarra(String codigoBarra);

        boolean existsByCodigo(String codigo);

        boolean existsByCodigoAndIdNot(String codigo, Long id);

        Page<Catalogo> findByTipoIn(java.util.Collection<Catalogo.Tipo> tipos, Pageable pageable);

        Page<Catalogo> findByEstadoAndTipoIn(Integer estado, java.util.Collection<Catalogo.Tipo> tipos, Pageable pageable);

        Page<Catalogo> findByTipoAtencion_IdAndTipoIn(Long idTipoAtencion, java.util.Collection<Catalogo.Tipo> tipos, Pageable pageable);

        @Query("SELECT c FROM Catalogo c WHERE c.tipo IN :tipos AND ("
                        +
                        "LOWER(c.nombreServicio) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(c.codigo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(c.codigoSeus) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(c.nivel.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
        Page<Catalogo> searchServicios(@Param("searchTerm") String searchTerm, @Param("tipos") java.util.Collection<Catalogo.Tipo> tipos, Pageable pageable);

        @Query("SELECT c FROM Catalogo c WHERE c.estado = :estado AND c.tipo IN :tipos AND ("
                        +
                        "(LOWER(c.nombreServicio) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(c.nivel.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(c.codigo) LIKE LOWER(CONCAT('%', :searchTerm, '%'))))")
        Page<Catalogo> searchServiciosByEstado(
                        @Param("searchTerm") String searchTerm,
                        @Param("estado") Integer estado,
                        @Param("tipos") java.util.Collection<Catalogo.Tipo> tipos,
                        Pageable pageable);

        @Query("SELECT c FROM Catalogo c WHERE c.estado = 1 AND c.tipo IN :tipos AND "
                        +
                        "(c.id IN :ids OR c.nivel.idNivel IN :niveles) AND " +
                        "(:term IS NULL OR :term = '' OR LOWER(c.nombreServicio) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :term, '%')))")
        Page<Catalogo> searchServiciosConReglas(
                        @Param("term") String term,
                        @Param("ids") java.util.List<Long> ids,
                        @Param("niveles") java.util.List<Long> niveles,
                        @Param("tipos") java.util.Collection<Catalogo.Tipo> tipos,
                        Pageable pageable);

        @Query(value = "SELECT MAX(codigo) FROM catalogo WHERE codigo ~ '^[0-9]{7}$'", nativeQuery = true)
        String findMaxCodigo();

        @Query(value = "SELECT MAX(CAST(codigo AS INTEGER)) FROM catalogo " +
                        "WHERE codigo ~ '^[0-9]{7}$'", nativeQuery = true)
        Integer findMaxNumericCode();

        @Query(value = "SELECT COUNT(*) FROM catalogo WHERE codigo ~ '^[0-9]{7}$'", nativeQuery = true)
        Long countServiciosWithNumericCode();

        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Catalogo c " +
                        "WHERE c.codigo >= :minCode AND c.codigo <= :maxCode")
        boolean existsCodigoInRange(String minCode, String maxCode);

        @Query("SELECT COUNT(c) FROM Catalogo c WHERE c.estado = 1")
        Long countActiveServicios();

        Long countByTipo(Catalogo.Tipo tipo);

        @Query("SELECT COUNT(c) FROM Catalogo c WHERE c.nivel.idNivel = :nivelId")
        Long countByNivelId(Long nivelId);

}
