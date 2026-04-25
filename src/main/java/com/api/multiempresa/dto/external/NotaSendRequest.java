package com.api.multiempresa.dto.external;

import lombok.Data;

@Data
public class NotaSendRequest {

  private String notaSerieModifica;
  private Integer notaNumeroModifica;
  private String notaFechaModifica;

  /**
   * Nombre del enum Catalog09_10TipoNotaCreditoDebito del facturador.
   * Ej: CREDITO_DEVOLUCION_TOTAL, DEBITO_AUMENTO_VALOR
   */
  private String tipoNotaCreditoDebito;

  /**
   * Nombre del enum Catalog01TipoDocumento del facturador.
   * Valores posibles: FACTURA, BOLETA
   */
  private String tipoDocumentoAfecta;
}
