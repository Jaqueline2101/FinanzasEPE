package com.pe.finanzas.compra.scheduler;

import com.pe.finanzas.compra.entity.CronogramaPago;
import com.pe.finanzas.compra.repository.CronogramaPagoRepository;
import com.pe.finanzas.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CronogramaPagoScheduler {

    private final CronogramaPagoRepository cronogramaPagoRepository;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Tarea programada que se ejecuta diariamente a las 8:00 AM.
     * Busca cuotas que vencen en 15 días para notificar al usuario.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void notificarVencimientosProximos() {
        log.info(">>> Iniciando verificación de vencimientos de pagos a 15 días");
        
        LocalDate fechaObjetivo = LocalDate.now().plusDays(15);
        // Estado 1 o 2 suele ser PENDIENTE en este sistema. 16 es PAGADO.
        // Buscamos las que NO están pagadas (estado != 16)
        List<CronogramaPago> cuotasProximas = cronogramaPagoRepository.findByFechaVencimientoAndEstado(fechaObjetivo, 1);
        
        // También buscamos estado 2 por si acaso (PENDIENTE)
        cuotasProximas.addAll(cronogramaPagoRepository.findByFechaVencimientoAndEstado(fechaObjetivo, 2));

        if (cuotasProximas.isEmpty()) {
            log.info("No se encontraron cuotas que venzan el {}", fechaObjetivo);
            return;
        }

        log.info("Se encontraron {} cuotas que vencen el {}. Enviando notificaciones...", cuotasProximas.size(), fechaObjetivo);

        for (CronogramaPago cuota : cuotasProximas) {
            String proveedor = cuota.getCompra().getProveedor().getRazonSocial();
            String serie = cuota.getCompra().getSerie();
            String titulo = "Vencimiento Próximo: " + proveedor;
            String mensaje = String.format("La cuota #%d del documento %s vence el %s (en 15 días). Monto: S/ %.2f",
                    cuota.getNumeroCuota(), serie, cuota.getFechaVencimiento(), cuota.getMontoCuota());

            // Enviamos la notificación vía Kafka
            // El userId se deja vacío para que sea un broadcast o se podría enviar a un rol administrativo
            kafkaProducerService.enviarNotificacion(null, "CUENTAS_PAGAR", titulo, mensaje, "WARNING");
        }
        
        log.info("<<< Finalización de notificaciones de vencimiento");
    }
}
