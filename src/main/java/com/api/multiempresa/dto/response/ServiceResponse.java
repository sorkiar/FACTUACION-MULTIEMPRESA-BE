package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ServiceResponse {
  private Long id;
  private String sku;
  private String name;
  private Long serviceCategoryId;
  private String serviceCategoryName;
  private Long chargeUnitId;
  private String chargeUnitName;

  private Long detractionId;
  private String detractionCode;
  private String detractionDescription;
  private BigDecimal detractionPercentage;
  private BigDecimal detractionMinAmount;
  private BigDecimal pricePen;
  private BigDecimal priceUsd;
  private String estimatedTime;
  private String expectedDelivery;
  private Boolean requiresMaterials;
  private Boolean requiresSpecification;
  private String includesDescription;
  private String excludesDescription;
  private String conditions;
  private String shortDescription;
  private String detailedDescription;
  private String imageUrl;
  private String technicalSheetUrl;
  private Integer status;
  private LocalDateTime registrationDate;
}
