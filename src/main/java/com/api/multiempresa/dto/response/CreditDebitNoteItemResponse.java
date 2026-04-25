package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreditDebitNoteItemResponse {
  private Long id;
  private String itemType;
  private Long productId;
  private Long serviceId;
  private String description;
  private BigDecimal quantity;
  private BigDecimal unitPrice;
  private BigDecimal discountPercentage;
  private BigDecimal subtotalAmount;
  private BigDecimal taxAmount;
  private BigDecimal totalAmount;
}
