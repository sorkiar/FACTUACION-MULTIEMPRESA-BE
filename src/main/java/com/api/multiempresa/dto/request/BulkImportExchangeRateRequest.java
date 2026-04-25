package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class BulkImportExchangeRateRequest {

  @NotNull(message = "from es requerido")
  private LocalDate from;

  @NotNull(message = "to es requerido")
  private LocalDate to;
}
