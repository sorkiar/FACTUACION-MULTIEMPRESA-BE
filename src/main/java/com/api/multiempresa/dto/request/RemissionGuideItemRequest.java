package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RemissionGuideItemRequest {

  /** Referencia opcional al catálogo de productos */
  private Long productId;

  @NotBlank(message = "description es obligatorio")
  private String description;

  @NotNull(message = "quantity es obligatorio")
  @DecimalMin(value = "0.001", message = "quantity debe ser mayor a 0")
  private BigDecimal quantity;

  /** Código SUNAT de unidad de medida (ej: NIU, KGM, ZZ). Default NIU. */
  private String unitMeasureSunat = "NIU";

  /** Precio unitario con IGV */
  @NotNull(message = "unitPrice es obligatorio")
  @DecimalMin(value = "0.00", message = "unitPrice no puede ser negativo")
  private BigDecimal unitPrice;
}
