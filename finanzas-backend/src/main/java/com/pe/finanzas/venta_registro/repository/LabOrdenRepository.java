package com.pe.finanzas.venta_registro.repository;

import com.pe.finanzas.rep_empresas_atendidas.dto.EmpresaAtendidaDTO;
import com.pe.finanzas.venta_registro.entity.LabOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LabOrdenRepository extends JpaRepository<LabOrden, Long> {

    List<LabOrden> findByEstado(String estado);

    List<LabOrden> findByIdPacienteAndEstado(Long idPaciente, String estado);

    @Query("SELECT o FROM LabOrden o WHERE o.estado = 'PENDIENTE' ORDER BY o.fecha DESC")
    List<LabOrden> findOrdenesPendientes();

    @Query("SELECT o FROM LabOrden o WHERE o.idPaciente = :idPaciente AND o.estado = 'PENDIENTE' ORDER BY o.fecha DESC")
    List<LabOrden> findOrdenesPendientesPorPaciente(@Param("idPaciente") Long idPaciente);

    @Query("SELECT new com.pe.finanzas.rep_empresas_atendidas.dto.EmpresaAtendidaDTO(" +
            "dp.idCompania, " +
            "c.nombre, " +
            "COUNT(o), " +
            "COALESCE(SUM(o.totalEstimado), 0) " +
            ") " +
            "FROM LabOrden o, DatosPersonales dp " +
            "LEFT JOIN Compania c ON dp.idCompania = c.id " +
            "WHERE o.idPaciente = dp.id " +
            "AND dp.idCompania IS NOT NULL " +
            "AND o.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "GROUP BY dp.idCompania, c.nombre " +
            "ORDER BY COUNT(o) DESC")
    List<EmpresaAtendidaDTO> findEmpresasMasAtendidas(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}
