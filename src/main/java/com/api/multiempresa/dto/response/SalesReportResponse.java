package com.api.multiempresa.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesReportResponse {
  private String companyName;
  private String dateRange;
  private int totalItems;
  private List<SalesReportRowResponse> rows;
}
