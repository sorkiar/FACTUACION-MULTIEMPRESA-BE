package com.api.multiempresa.dto.filter;

import lombok.Data;

@Data
public class DocumentSeriesFilter {
  private Long id;
  private Integer status;
  private String documentTypeCode;
  private String series;
}
