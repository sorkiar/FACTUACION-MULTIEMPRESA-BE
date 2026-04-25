package com.api.multiempresa.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CreditDebitNoteRequest {

  @NotNull(message = "saleId es obligatorio")
  private Long saleId;

  @NotNull(message = "originalDocumentId es obligatorio")
  private Long originalDocumentId;

  /**
   * Código del tipo de nota según Catálogo 09/10 de SUNAT:
   * Crédito: C01, C02, C03, C04, C05, C06, C07, C08, C09, C10, C11, C12, C13
   * Débito:  D01, D02, D03, D11, D12
   */
  @NotBlank(message = "noteTypeCode es obligatorio")
  private String noteTypeCode;

  private String reason;

  @NotNull(message = "documentSeriesId es obligatorio")
  private Long documentSeriesId;

  @NotEmpty(message = "Debe registrar al menos un ítem")
  @Valid
  private List<CreditDebitNoteItemRequest> items;
}
