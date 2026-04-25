package com.api.multiempresa.dto.response;

import lombok.Data;

@Data
public class DocumentSeriesResponse {
  private Long id;
  private String documentTypeSunatCode;
  private String documentTypeSunatName;
  private String series;
  private Integer sequence;
  private Integer status;
}
