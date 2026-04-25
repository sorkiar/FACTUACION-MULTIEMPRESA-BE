package com.api.multiempresa.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SunatSendConfigRequest {

  @Valid private DocumentSendConfigInput boleta;
  @Valid private DocumentSendConfigInput factura;
  @Valid private DocumentSendConfigInput notaCredito;
  @Valid private DocumentSendConfigInput notaDebito;
  @Valid private DocumentSendConfigInput guiaRemision;

  @Data
  public static class DocumentSendConfigInput {

    @Pattern(regexp = "ONLINE|OFFLINE", message = "El modo debe ser ONLINE u OFFLINE")
    private String modo;

    @Min(value = 1, message = "El intervalo mínimo es 1 minuto")
    @Max(value = 1440, message = "El intervalo máximo es 1440 minutos (24 h)")
    private Integer intervaloMinutos;
  }
}
