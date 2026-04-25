package com.api.multiempresa.dto.external;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ItemSendRequest {
  private String itcoUnidadMedida;
  private String itcoDescripcion;
  private BigDecimal itcoCantidad;
  private BigDecimal itcoValorUnitario;
  private BigDecimal itcoPrecioUnitario;
  private BigDecimal itcoDescuentoAfecta = BigDecimal.ZERO;
  private BigDecimal itcoSubTotal;
  private BigDecimal itcoIgv;
  private BigDecimal itcoTotal;
  private BigDecimal itcoRetencion = BigDecimal.ZERO;
  private String tipoAfectacionIgv;
}
