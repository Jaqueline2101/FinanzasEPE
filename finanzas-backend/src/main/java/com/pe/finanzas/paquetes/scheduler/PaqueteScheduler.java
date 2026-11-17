package com.pe.finanzas.paquetes.scheduler;

import com.pe.finanzas.paquetes.service.PaqueteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaqueteScheduler {

    private final PaqueteService paqueteService;

    /**
     * Ejecutar diariamente a las 00:00:01 AM
     * Cron: Seg Min Hora Dia Mes DiaSemana
     */
    @Scheduled(cron = "1 0 0 * * ?")
    public void procesarVencimientosDiarios() {
        log.info(">>> Iniciando tarea programada: Verificación de paquetes vencidos");
        try {
            paqueteService.verificarVencimientoPaquetes();
        } catch (Exception e) {
            log.error("Error en la tarea programada de paquetes: ", e);
        }
        log.info("<<< Tarea programada finalizada");
    }
}
