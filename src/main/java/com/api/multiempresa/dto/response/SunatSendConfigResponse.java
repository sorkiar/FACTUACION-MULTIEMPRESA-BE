package com.api.multiempresa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SunatSendConfigResponse {

  private DocumentSendConfig boleta;
  private DocumentSendConfig factura;
  private DocumentSendConfig notaCredito;
  private DocumentSendConfig notaDebito;
  private DocumentSendConfig guiaRemision;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DocumentSendConfig {
    /** ONLINE: se envía al instante en la creación del comprobante.
     *  OFFLINE: el job programado gestiona el envío según el intervalo configurado. */
    private String modo;

    /** Intervalo en minutos entre ejecuciones del job (aplica solo en modo OFFLINE). */
    private Integer intervaloMinutos;
  }
}
