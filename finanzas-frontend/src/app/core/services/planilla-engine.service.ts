import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class PlanillaEngineService {

    // Constantes de Ley
    private readonly UIT_AÑO = 5150;
    private readonly TASA_ONP = 0.13;
    private readonly APORTE_OBLIGATORIO_AFP = 0.10;

    constructor() { }

    /**
     * Calcula la Base Asegurable (Remuneración computable para AFP/ONP y 5ta)
     * No incluye gratificaciones ni CTS (que no son parte de este pago mensual usual).
     */
    calcularBaseAsegurable(ingresos: any): number {
        return (ingresos.sueldoBase || 0) +
            (ingresos.horasExtras || 0) +
            (ingresos.comisiones || 0) +
            (ingresos.bonificaciones || 0) +
            (ingresos.asignacionFamiliar || 0) +
            (ingresos.vacaciones || 0);
    }

    /**
     * Calcula el Descuento por Sistema Pensionario
     */
    calcularAfpOnp(baseAsegurable: number, config: any): number {
        if (!baseAsegurable || baseAsegurable <= 0) return 0;

        if (config.sistemaPensionario === 'ONP') {
            return baseAsegurable * this.TASA_ONP;
        }

        if (config.sistemaPensionario === 'AFP') {
            // AFP Formula: 10% obligatorio + % Comisión + % Prima de Seguro
            const primaSeguro = (config.porcentajeSeguro || 0) / 100;
            const comision = (config.porcentajeComision || 0) / 100;
            const tasaTotalAfp = this.APORTE_OBLIGATORIO_AFP + comision + primaSeguro;

            return baseAsegurable * tasaTotalAfp;
        }

        return 0; // NINGUNO
    }

    /**
     * Proyección simplificada de 5ta Categoría (Mensual)
     * Nota: Este es un cálculo referencial de UX. El cálculo real debe contemplar 
     * retenciones históricas del año, gratificaciones proyectadas, etc.
     */
    calcularQuintaCategoria(baseMensual: number, mesesRestantes: number = 12): number {
        if (!baseMensual || baseMensual <= 0) return 0;

        // Proyectamos el año (Sueldo * 12 + 2 Gratificaciones)
        // Simplificado: Sueldo * 14
        const rentaAnualProyectada = baseMensual * 14;

        // Deducción de 7 UIT
        const deduccion7UIT = 7 * this.UIT_AÑO;
        let rentaNetaAnual = rentaAnualProyectada - deduccion7UIT;

        if (rentaNetaAnual <= 0) return 0; // No paga 5ta

        // Escalas (Tramos de UIT)
        // Hasta 5 UIT (8%)
        // De 5 a 20 UIT (14%)
        // De 20 a 35 UIT (17%)
        // De 35 a 45 UIT (20%)
        // Más de 45 UIT (30%)

        let impuestoAnual = 0;
        const uit = this.UIT_AÑO;

        const tramo1 = 5 * uit;
        const tramo2 = 20 * uit;
        const tramo3 = 35 * uit;
        const tramo4 = 45 * uit;

        if (rentaNetaAnual <= tramo1) {
            impuestoAnual = rentaNetaAnual * 0.08;
        } else if (rentaNetaAnual <= tramo2) {
            impuestoAnual = (tramo1 * 0.08) + ((rentaNetaAnual - tramo1) * 0.14);
        } else if (rentaNetaAnual <= tramo3) {
            impuestoAnual = (tramo1 * 0.08) + ((tramo2 - tramo1) * 0.14) + ((rentaNetaAnual - tramo2) * 0.17);
        } else if (rentaNetaAnual <= tramo4) {
            impuestoAnual = (tramo1 * 0.08) + ((tramo2 - tramo1) * 0.14) + ((tramo3 - tramo2) * 0.17) + ((rentaNetaAnual - tramo3) * 0.20);
        } else {
            impuestoAnual = (tramo1 * 0.08) + ((tramo2 - tramo1) * 0.14) + ((tramo3 - tramo2) * 0.17) + ((tramo4 - tramo3) * 0.20) + ((rentaNetaAnual - tramo4) * 0.30);
        }

        // Dividir el impuesto anual proyectado entre 12 meses
        return impuestoAnual / 12;
    }
}
