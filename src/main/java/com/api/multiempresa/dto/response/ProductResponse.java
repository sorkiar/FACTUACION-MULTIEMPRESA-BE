package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductResponse {
  private Long id;
  private String sku;
  private String name;

  private Long categoryId;
  private String categoryName;

  private Long unitMeasureId;
  private String unitMeasureCode;

  private Long detractionId;
  private String detractionCode;
  private String detractionDescription;
  private BigDecimal detractionPercentage;
  private BigDecimal detractionMinAmount;

  private BigDecimal salePricePen;
  private BigDecimal estimatedCostPen;
  private BigDecimal salePriceUsd;
  private BigDecimal estimatedCostUsd;

  private String brand;
  private String model;

  private String shortDescription;
  private String technicalSpec;

  private String mainImageUrl;
  private String technicalSheetUrl;

  private Integer status;
}
