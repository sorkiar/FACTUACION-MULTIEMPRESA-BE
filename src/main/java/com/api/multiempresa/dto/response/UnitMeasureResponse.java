package com.api.multiempresa.dto.response;

import lombok.Data;

@Data
public class UnitMeasureResponse {
  private Long id;
  private String code;
  private String name;
  private String symbol;
  private Integer status;
}
