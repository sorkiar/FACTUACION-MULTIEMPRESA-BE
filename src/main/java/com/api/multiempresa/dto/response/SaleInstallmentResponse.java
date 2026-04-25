package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SaleInstallmentResponse {
  private Long id;
  private Integer installmentNumber;
  private String dueDate;
  private BigDecimal amount;
}
