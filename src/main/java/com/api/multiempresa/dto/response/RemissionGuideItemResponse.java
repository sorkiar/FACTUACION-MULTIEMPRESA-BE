package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class RemissionGuideItemResponse {

  private Long id;
  private Long productId;
  private String description;
  private BigDecimal quantity;
  private String unitMeasureSunat;
  private BigDecimal unitPrice;
  private BigDecimal subtotalAmount;
  private BigDecimal taxAmount;
  private BigDecimal totalAmount;
}
