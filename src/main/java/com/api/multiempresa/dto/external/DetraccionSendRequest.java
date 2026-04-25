package com.api.multiempresa.dto.external;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DetraccionSendRequest {
  private String detrCuenta;
  private BigDecimal detrPorcentaje;
  private BigDecimal detrMonto;
  private Boolean detrEstado = true;
  private TipoDetraccionSendRequest tipoDetraccion;
}
