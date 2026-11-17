package com.pe.finanzas.motor_precios.repository;

import com.pe.finanzas.motor_precios.entity.CredPlanDescuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredPlanDescuentoRepository extends JpaRepository<CredPlanDescuento, Long> {

        @Query("SELECT cpd FROM CredPlanDescuento cpd WHERE cpd.plan.idPlan = :idPlan AND cpd.estado = '1'")
        List<CredPlanDescuento> findActivosByPlan(@Param("idPlan") Long idPlan);

        @Query("SELECT cpd FROM CredPlanDescuento cpd WHERE cpd.plan.idPlan = :idPlan " +
                        "AND cpd.tipoAtencion = :tipoAtencion AND cpd.estado = '1'")
        List<CredPlanDescuento> findByPlanAndTipoAtencion(
                        @Param("idPlan") Long idPlan,
                        @Param("tipoAtencion") String tipoAtencion);

        @Query("SELECT cpd FROM CredPlanDescuento cpd WHERE cpd.plan.idPlan = :idPlan " +
                        "AND cpd.idServicio = :idServicio " +
                        "AND cpd.estado = '1'")
        Optional<CredPlanDescuento> findByPlanAndServicio(
                        @Param("idPlan") Long idPlan,
                        @Param("idServicio") Long idServicio);

        @Query("SELECT cpd FROM CredPlanDescuento cpd WHERE cpd.plan.idPlan = :idPlan " +
                        "AND cpd.tipoAtencion = :tipoAtencion " +
                        "AND (cpd.nivelServicio = :nivel OR cpd.nivelServicio = 0 OR cpd.nivelServicio IS NULL) " +
                        "AND cpd.estado = '1'")
        List<CredPlanDescuento> findByPlanTipoAtencionAndNivel(
                        @Param("idPlan") Long idPlan,
                        @Param("tipoAtencion") String tipoAtencion,
                        @Param("nivel") Integer nivel);

        void deleteByPlanIdPlan(Long idPlan);

        @Query("SELECT cpd FROM CredPlanDescuento cpd WHERE cpd.plan.idPlan = :idPlan " +
                        "AND cpd.nivelServicio = :nivelServicio AND cpd.idServicio IS NULL")
        Optional<CredPlanDescuento> findByPlanIdPlanAndNivelServicio(
                        @Param("idPlan") Long idPlan,
                        @Param("nivelServicio") Integer nivelServicio);

        @Modifying
        @Query("DELETE FROM CredPlanDescuento cpd WHERE cpd.plan.idPlan = :idPlan " +
                        "AND cpd.idServicio IS NOT NULL " +
                        "AND cpd.idServicio IN (SELECT c.id FROM Catalogo c WHERE c.nivel.idNivel = cast(:nivelServicio as long))")
        void deleteIndividualesByPlanAndNivel(
                        @Param("idPlan") Long idPlan,
                        @Param("nivelServicio") Integer nivelServicio);
}
