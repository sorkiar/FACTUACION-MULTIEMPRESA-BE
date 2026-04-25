package com.api.multiempresa.dto.filter;

import lombok.Data;

@Data
public class DetractionCodeFilter {
  private String code;
  private String category;
  private Integer status;
}
