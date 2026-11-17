export interface ReporteResumenDto {
  totalBruto: number;
  totalNeto: number;
  totalDescuentos: number;
  cantidadPagos: number;
}

export interface ReporteGraficoDto {
  etiqueta: string;
  valor: number;
  cantidad: number;
}

export interface DashboardPagoPersonalDto {
  resumen: ReporteResumenDto;
  mensual: ReporteGraficoDto[];
  area: ReporteGraficoDto[];
}
