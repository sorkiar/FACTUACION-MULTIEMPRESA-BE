package com.api.multiempresa.dto.filter;

import java.time.LocalDate;
import lombok.Data;

@Data
public class SaleFilter {
  private Long id;
  private Long clientId;
  private String saleStatus;
  private String paymentStatus;
  private LocalDate startDate;
  private LocalDate endDate;
  private Boolean excludeAnnulled;
}
