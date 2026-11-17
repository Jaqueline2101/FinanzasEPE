package com.pe.finanzas.jobs;

import com.pe.finanzas.motor_precios.entity.CredPlan;
import com.pe.finanzas.motor_precios.repository.CredPlanRepository;
import com.pe.finanzas.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanExpirationJob {

    private final CredPlanRepository planRepository;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Se ejecuta todos los días a las 00:05 AM para desactivar planes vencidos
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void deactivateExpiredPlans() {
        log.info("Iniciando proceso de desactivación automática de planes vencidos...");
        
        LocalDate today = LocalDate.now();
        // Buscamos los que se van a desactivar para notificar
        List<CredPlan> toDeactivate = planRepository.findExpiredActivePlans(today);
        
        int count = planRepository.deactivateExpiredPlans(today);
        
        if (count > 0) {
            log.info("Se han desactivado {} planes por vencimiento de fecha.", count);
            for (CredPlan plan : toDeactivate) {
                kafkaProducerService.enviarNotificacion(
                        null,
                        "PLANES",
                        "Plan Desactivado (Expirado)",
                        "El plan '" + plan.getNombre() + "' ha vencido y fue desactivado automáticamente.",
                        "error"
                );
            }
        } else {
            log.info("No se encontraron planes para desactivar hoy.");
        }
    }

    /**
     * Se ejecuta todos los días a las 08:00 AM
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkPlanExpirations() {
        log.info("Iniciando revisión diaria de vencimientos de planes...");
        
        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);
        LocalDate in5Days = today.plusDays(5);

        // Notificaciones para 30 días
        List<CredPlan> expiringIn30 = planRepository.findByFechaFinAndEstado(in30Days, "1");
        notifyExpirations(expiringIn30, "Vencimiento Próximo (30 días)");

        // Notificaciones para 5 días
        List<CredPlan> expiringIn5 = planRepository.findByFechaFinAndEstado(in5Days, "1");
        notifyExpirations(expiringIn5, "Vencimiento Crítico (5 días)");
    }

    private void notifyExpirations(List<CredPlan> plans, String titulo) {
        for (CredPlan plan : plans) {
            String mensaje = String.format("El plan '%s' vencerá el %s. Por favor, tome las medidas necesarias.", 
                    plan.getNombre(), plan.getFechaFin());
            
            log.info("Notificando vencimiento para plan: {} ({})", plan.getNombre(), titulo);
            
            kafkaProducerService.enviarNotificacion(
                    null, // Para todos los usuarios del sistema
                    "PLANES",
                    titulo,
                    mensaje,
                    titulo.contains("Crítico") ? "warning" : "info"
            );
        }
    }
}
