package com.api.multiempresa.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FacturacionResponse {
  private FacturacionData data;
  private boolean success;
  private String message;

  @Data
  public static class FacturacionData {

    @JsonProperty("typeResultadoDeclaracion")
    private String declarationResultType;

    @JsonProperty("responseCode")
    private Integer responseCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("codigoHash")
    private String hashCode;

    @JsonProperty("cadenaQr")
    private String qrCode;

    @JsonProperty("base64Xml")
    private String xmlBase64;

    @JsonProperty("base64XmlCdr")
    private String cdrBase64;

    @JsonProperty("ticket")
    private String ticket;

    @JsonProperty("observacion")
    private String observation;
  }
}
