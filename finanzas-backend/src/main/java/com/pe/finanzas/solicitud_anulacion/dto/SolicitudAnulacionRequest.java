package com.pe.finanzas.solicitud_anulacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolicitudAnulacionRequest {

    /** ID de la venta que se desea anular (fecha anterior a hoy) */
    @NotNull(message = "El ID de venta es obligatorio")
    private Long idVenta;

    /** Sucursal a la que pertenece la venta */
    @NotNull(message = "El ID de sucursal es obligatorio")
    private Long idSucursal;

    /** Usuario que realiza la solicitud (cajero/vendedor) */
    @NotNull(message = "El ID del solicitante es obligatorio")
    private Long idSolicitante;

    /** Motivo de la solicitud — exigencia de auditoría */
    @NotBlank(message = "El motivo de la solicitud es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivoSolicitud;
}
