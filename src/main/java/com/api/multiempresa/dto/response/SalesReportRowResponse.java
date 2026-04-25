package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesReportRowResponse {
  private String issueDate;
  private String document;
  private String client;
  private String itemDescription;
  private BigDecimal quantity;
  private BigDecimal unitPrice;
  private BigDecimal discountPercentage;
  private BigDecimal subtotal;
  private BigDecimal saleTotal;
  private String currencyCode;
}
