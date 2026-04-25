package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SaleInstallmentRequest {

  @NotNull
  @Positive
  private Integer installmentNumber;

  @NotNull
  private String dueDate;

  @NotNull
  private BigDecimal amount;
}
