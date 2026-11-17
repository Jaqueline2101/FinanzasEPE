package com.pe.finanzas.paquetes.repository;

import com.pe.finanzas.paquetes.entity.Paquete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para consultas de paquetes
 */
@Repository
public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

        /**
         * Buscar paquete por código
         */
        Optional<Paquete> findByCodigo(String codigo);

        /**
         * Listar paquetes activos
         */
        List<Paquete> findByEstado(String estado);

        /**
         * Buscar paquetes vencidos para desactivación automática
         */
        List<Paquete> findByEstadoAndFechaFinBefore(String estado, LocalDate fecha);

        /**
         * Listar paquetes vigentes en una fecha
         */
        @Query("SELECT p FROM Paquete p WHERE p.estado = '1' " +
                        "AND (:fecha >= p.fechaInicio OR p.fechaInicio IS NULL) " +
                        "AND (:fecha <= p.fechaFin OR p.fechaFin IS NULL)")
        List<Paquete> findVigentesEnFecha(@Param("fecha") LocalDate fecha);

        /**
         * Listar paquetes vigentes hoy
         */
        @Query("SELECT p FROM Paquete p WHERE p.estado = '1' " +
                        "AND (CURRENT_DATE >= p.fechaInicio OR p.fechaInicio IS NULL) " +
                        "AND (CURRENT_DATE <= p.fechaFin OR p.fechaFin IS NULL)")
        List<Paquete> findVigentesHoy();

        /**
         * Verificar si existe paquete con código
         */
        boolean existsByCodigo(String codigo);

        /**
         * Listar paquetes por categoría paginado
         */
        Page<Paquete> findByCategoria(String categoria, Pageable pageable);
}
