package com.api.multiempresa.dto.filter;

import java.time.LocalDate;
import lombok.Data;

@Data
public class RemissionGuideFilter {

  private Long id;
  private String series;
  private String status;
  private Long transferReasonId;
  private String transportMode;
  private LocalDate startDate;
  private LocalDate endDate;
}
