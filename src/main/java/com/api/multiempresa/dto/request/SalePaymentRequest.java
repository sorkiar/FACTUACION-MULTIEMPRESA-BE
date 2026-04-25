package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SalePaymentRequest {

  @NotNull
  private Integer paymentMethodId;

  @NotNull
  private BigDecimal amountPaid;

  private String paymentReference;

  private String proofKey; // clave para asociar archivo
}
