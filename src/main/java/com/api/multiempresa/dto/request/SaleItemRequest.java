package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SaleItemRequest {
  @NotBlank
  private String itemType; // PRODUCTO, SERVICIO, PERSONALIZADO

  private Long productId;
  private Long serviceId;

  @NotBlank
  private String description;

  @NotNull
  private BigDecimal quantity;

  @NotNull
  private BigDecimal unitPrice;

  private BigDecimal discountPercentage;

  private Long detractionCodeId;
}
