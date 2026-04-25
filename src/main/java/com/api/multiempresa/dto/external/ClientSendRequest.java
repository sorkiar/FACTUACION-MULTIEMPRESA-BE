package com.api.multiempresa.dto.external;

import lombok.Data;

@Data
public class ClientSendRequest {

  private String clieNumeroDocumento;
  private String clieRazonSocial;
  private String clieDireccion;
  private String clieEmail1;
  private String clieEmail2;

  /**
   * Código SUNAT:
   * 1 = DNI
   * 6 = RUC
   * 4 = CE
   * etc.
   */
  private String tipoDocumentoIdentidad;
}
