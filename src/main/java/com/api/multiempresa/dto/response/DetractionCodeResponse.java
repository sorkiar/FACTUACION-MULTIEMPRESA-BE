package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DetractionCodeResponse {
  private Long id;
  private String code;
  private String description;
  private BigDecimal percentage;
  private BigDecimal minAmount;
  private String category;
  private Integer status;
}
