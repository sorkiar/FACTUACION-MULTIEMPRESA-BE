package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreditDebitNoteItemRequest {

  @NotBlank(message = "itemType es obligatorio")
  private String itemType; // PRODUCTO, SERVICIO, PERSONALIZADO

  private Long productId;

  private Long serviceId;

  @NotBlank(message = "description es obligatorio")
  private String description;

  @NotNull(message = "quantity es obligatorio")
  @Positive(message = "quantity debe ser positivo")
  private BigDecimal quantity;

  @NotNull(message = "unitPrice es obligatorio")
  @Positive(message = "unitPrice debe ser positivo")
  private BigDecimal unitPrice;

  private BigDecimal discountPercentage;
}
