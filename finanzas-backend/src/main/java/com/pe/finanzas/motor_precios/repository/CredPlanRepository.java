package com.pe.finanzas.motor_precios.repository;

import com.pe.finanzas.motor_precios.entity.CredPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CredPlanRepository extends JpaRepository<CredPlan, Long> {

        @Query("SELECT cp FROM CredPlan cp WHERE cp.idCompania = :idCompania ORDER BY cp.idPlan DESC")
        List<CredPlan> findByCompania(@Param("idCompania") Long idCompania);

        @Query("SELECT p FROM CredPlan p WHERE p.idCompania = :idCompania AND p.estado = '1' " +
                        "AND (p.fechaInicio <= :fecha OR p.fechaInicio IS NULL) " +
                        "AND (p.fechaFin >= :fecha OR p.fechaFin IS NULL)")
        List<CredPlan> findVigentesByCompania(@Param("idCompania") Long idCompania, @Param("fecha") LocalDate fecha);

        @Query("SELECT p FROM CredPlan p WHERE p.idEmpresa = :idEmpresa AND p.estado = '1' " +
                        "AND (p.fechaInicio <= :fecha OR p.fechaInicio IS NULL) " +
                        "AND (p.fechaFin >= :fecha OR p.fechaFin IS NULL)")
        List<CredPlan> findVigentesByEmpresa(@Param("idEmpresa") Long idEmpresa, @Param("fecha") LocalDate fecha);

        @Query("SELECT cp FROM CredPlan cp")
        List<CredPlan> findAllPlans();

        @Query("SELECT p FROM CredPlan p WHERE p.idCompania = :idCompania AND p.estado = '1'")
        Optional<CredPlan> findPlanActivoPorCompania(@Param("idCompania") Long idCompania);

        @Query("SELECT p FROM CredPlan p WHERE p.idEmpresa = :idEmpresa AND p.estado = '1'")
        Optional<CredPlan> findPlanActivoPorEmpresa(@Param("idEmpresa") Long idEmpresa);

        @Query("SELECT DISTINCT p.idEmpresa FROM CredPlan p WHERE p.idCompania = :idCompania AND p.idEmpresa IS NOT NULL")
        List<Long> findEmpresaIdsByCompania(@Param("idCompania") Long idCompania);

        Optional<CredPlan> findTopByIdCompaniaOrderByIdPlanDesc(Long idCompania);

        long countByIdCompania(Long idCompania);

        List<CredPlan> findByTipoPlanOrderByIdPlanDesc(String tipoPlan);

        Optional<CredPlan> findByCodigo(String codigo);

        List<CredPlan> findByFechaFinAndEstado(LocalDate fechaFin, String estado);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        @org.springframework.data.jpa.repository.Query("UPDATE CredPlan p SET p.estado = '0' WHERE p.fechaFin < :today AND p.estado = '1'")
        int deactivateExpiredPlans(@org.springframework.data.repository.query.Param("today") java.time.LocalDate today);

        @org.springframework.data.jpa.repository.Query("SELECT p FROM CredPlan p WHERE p.fechaFin < :today AND p.estado = '1'")
        List<CredPlan> findExpiredActivePlans(@org.springframework.data.repository.query.Param("today") java.time.LocalDate today);
}
