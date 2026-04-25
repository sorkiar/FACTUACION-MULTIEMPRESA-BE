package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRevenueResponse {

  private int year;
  private int month;
  private List<Integer> categories;
  private List<BigDecimal> series;
  private BigDecimal totalPen;
  private BigDecimal totalUsd;
}
