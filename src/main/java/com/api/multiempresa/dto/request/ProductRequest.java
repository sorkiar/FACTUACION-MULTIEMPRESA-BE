package com.api.multiempresa.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequest {
  @NotBlank(message = "El nombre es obligatorio")
  private String name;

  @NotNull(message = "La categoría es obligatoria")
  private Long categoryId;

  @NotNull(message = "La unidad de medida es obligatoria")
  private Long unitMeasureId;

  // Al menos uno de los dos precios de venta debe ser > 0 (validado en el servicio)
  @DecimalMin(value = "0.00", message = "El precio en soles no puede ser negativo")
  private BigDecimal salePricePen;

  @DecimalMin(value = "0.00", message = "El precio en dólares no puede ser negativo")
  private BigDecimal salePriceUsd;

  @DecimalMin(value = "0.00", message = "El costo en soles no puede ser negativo")
  private BigDecimal estimatedCostPen;

  @DecimalMin(value = "0.00", message = "El costo en dólares no puede ser negativo")
  private BigDecimal estimatedCostUsd;

  private Long detractionCodeId;

  private String brand;
  private String model;

  @NotBlank(message = "La descripción corta es obligatoria")
  private String shortDescription;

  @NotBlank(message = "La especificación técnica es obligatoria")
  private String technicalSpec;
}
