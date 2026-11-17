package com.pe.finanzas.niveles.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.pe.finanzas.niveles.entity.Nivel;

import java.util.List;
import java.util.Optional;

@Repository
public interface NivelRepository extends JpaRepository<Nivel, Long> {

        // Buscar por nombre
        Optional<Nivel> findByNombre(String nombre);

        Optional<Nivel> findByNombreIgnoreCase(String nombre);

        // Buscar niveles raíz (sin padre)
        List<Nivel> findByNivelPadreIsNullOrderByOrden();

        Page<Nivel> findByNivelPadreIsNull(Pageable pageable);

        // Buscar hijos directos de un nivel
        List<Nivel> findByNivelPadreIdNivelOrderByOrden(Long idNivelPadre);

        // **NUEVO: Buscar hijos ordenados por código numérico**
        List<Nivel> findByNivelPadreIdNivelOrderByNumNivel(Long idNivelPadre);

        // Buscar por nivel de jerarquía
        List<Nivel> findByNivelJerarquiaOrderByOrden(Integer nivelJerarquia);

        // Buscar por estado
        List<Nivel> findByEstadoOrderByOrden(Integer estado);

        // Buscar con filtros
        @Query("SELECT n FROM Nivel n WHERE " +
                        "(:nombre IS NULL OR LOWER(n.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
                        "(:tipo IS NULL OR n.tipo = :tipo) AND " +
                        "(:estado IS NULL OR n.estado = :estado) AND " +
                        "(:idNivelPadre IS NULL OR n.nivelPadre.idNivel = :idNivelPadre)")
        Page<Nivel> buscarConFiltros(
                        @Param("nombre") String nombre,
                        @Param("tipo") String tipo,
                        @Param("estado") Integer estado,
                        @Param("idNivelPadre") Long idNivelPadre,
                        Pageable pageable);

        // Obtener jerarquía completa desde un nivel (recursivo)
        @Query(value = "WITH RECURSIVE jerarquia AS (" +
                        "  SELECT * FROM niveles WHERE id_nivel = :idNivel AND deleted = false " +
                        "  UNION ALL " +
                        "  SELECT n.* FROM niveles n " +
                        "  INNER JOIN jerarquia j ON n.id_nivel_padre = j.id_nivel " +
                        "  WHERE n.deleted = false " +
                        ") SELECT * FROM jerarquia ORDER BY nivel_jerarquia, orden", nativeQuery = true)
        List<Nivel> obtenerJerarquiaCompleta(@Param("idNivel") Long idNivel);

        // Contar hijos directos
        Long countByNivelPadreIdNivel(Long idNivelPadre);

        // Verificar si existe por nombre y padre
        boolean existsByNombreIgnoreCaseAndNivelPadreIdNivel(String nombre, Long idNivelPadre);

        // **NUEVO: Verificar si existe código en niveles raíz**
        boolean existsByNumNivelAndNivelPadreIsNull(String numNivel);

        // **NUEVO: Verificar si existe código en general**
        boolean existsByNumNivel(String numNivel);

        // **NUEVO: Buscar por código**
        Optional<Nivel> findByNumNivel(String numNivel);

        // Obtener nivel máximo de jerarquía
        @Query("SELECT MAX(n.nivelJerarquia) FROM Nivel n")
        Integer obtenerNivelMaximoJerarquia();

        Long countByEstado(Integer estado);

        @Query("SELECT DISTINCT n FROM Nivel n WHERE EXISTS " +
                        "(SELECT 1 FROM Nivel h WHERE h.nivelPadre.idNivel = n.idNivel)")
        List<Nivel> findNivelesConHijos();

        @Query("SELECT n FROM Nivel n WHERE n.nivelPadre.idNivel = :idNivelPadre " +
                        "ORDER BY n.numNivel DESC")
        List<Nivel> findTopByNivelPadreIdNivelOrderByNumNivelDesc(@Param("idNivelPadre") Long idNivelPadre);
}
